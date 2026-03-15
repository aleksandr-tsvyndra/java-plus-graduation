package ru.practicum.core.event.service.impl;

import com.google.protobuf.Timestamp;
import feign.FeignException;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.core.api.exception.ConflictException;
import ru.practicum.core.api.exception.NotFoundException;
import ru.practicum.core.api.internal.event.dto.EventDto;
import ru.practicum.core.api.internal.request.client.RequestClient;
import ru.practicum.core.api.internal.request.dto.EventRequestsCountDto;
import ru.practicum.core.api.internal.user.client.UserClient;
import ru.practicum.core.api.internal.user.dto.UserShortDto;
import ru.practicum.core.api.util.enums.EventState;
import ru.practicum.core.event.dto.events.AdminEventParams;
import ru.practicum.core.event.dto.events.NewEventDto;
import ru.practicum.core.event.dto.events.UpdateEventAdminRequest;
import ru.practicum.core.event.dto.events.UpdateEventUserRequest;
import ru.practicum.core.event.dto.events.UserEventParams;
import ru.practicum.core.event.mapper.EventMapper;
import ru.practicum.core.event.model.Category;
import ru.practicum.core.event.model.Event;
import ru.practicum.core.event.model.enums.events.EventSort;
import ru.practicum.core.event.model.enums.events.EventStateAction;
import ru.practicum.core.event.repository.EventRepository;
import ru.practicum.core.event.service.api.CategoryService;
import ru.practicum.core.event.service.api.EventService;
import ru.practicum.recomm.client.AnalyzerClient;
import ru.practicum.recomm.client.CollectorClient;
import ru.practicum.recommendations.messages.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static ru.practicum.core.event.model.enums.events.EventSort.EVENT_DATE;
import static ru.practicum.core.event.repository.EventRepository.AdminEventSpec.withAdminParams;
import static ru.practicum.core.event.repository.EventRepository.UserEventSpec.withUserParams;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private static final String EVENT_STATE_ACTION_CONFLICT_MESSAGE = "Можно изменять только события в состоянии ОЖИДАНИЕ или ОТМЕНЕНО";
    private static final String GET_EVENT_ERROR_MESSAGE = "Событие с ID=%d не найдено";
    private static final String EVENT_NOT_PUBLISHED_ERROR_MESSAGE = "Событие с ID=%d не опубликовано";
    private static final String EVENT_NOT_OWNED_BY_USER_ERROR_MESSAGE = "Событие с ID=%d не соответствует инициатору с ID=%d";
    private static final String PUBLISH_NOT_PENDING_EVENT_ERROR_MESSAGE = "Нельзя опубликовать событие, которое не находится в состоянии ОЖИДАНИЕ";
    private static final String REJECT_PUBLISHED_EVENT_ERROR_MESSAGE = "Нельзя отменить не опубликованное событие";
    private static final String UNACCEPTABLE_ACTION_ON_EVENT_ERROR_MESSAGE = "Недопустимое действие %s над состоянием события";
    private static final String EARLY_START_ERROR_MESSAGE = "Дата начала события не может быть раньше чем через один час после публикации";
    private static final String USER_NOT_FOUND_ERROR_MESSAGE = "Пользователь с ID=%d не найден";
    private static final String USERS_NOT_FOUND_ERROR_MESSAGE = "Пользователи с ID=%s не найдены";
    private static final String RANGE_ERROR_MESSAGE = "Некорректный диапазон";

    private final EventMapper eventMapper;
    private final EventRepository eventRepository;
    private final CategoryService categoryService;

    private final CollectorClient collectorClient;
    private final AnalyzerClient analyzerClient;
    private final UserClient userClient;
    private final RequestClient requestClient;

    @Override
    public EventDto addEvent(NewEventDto newEventDto, Long userId) {
        // Получаем сущность пользователя из микросервиса user-service
        UserShortDto userShortDto = getUserOrThrow(userId);
        // Получаем сущность категории из сервиса
        Category category = categoryService.getCategoryById(newEventDto.getCategory());

        // Формируем модель события
        Event event = Event.builder()
                .title(newEventDto.getTitle())
                .annotation(newEventDto.getAnnotation())
                .description(newEventDto.getDescription())
                .category(category)
                .initiatorId(userShortDto.getId())
                .eventDate(newEventDto.getEventDate())
                .createdOn(LocalDateTime.now())
                .locationLat(newEventDto.getLocation().getLat())
                .locationLon(newEventDto.getLocation().getLon())
                .participantLimit(Objects.requireNonNullElse(newEventDto.getParticipantLimit(), 0))
                .paid(Objects.requireNonNullElse(newEventDto.getPaid(), false))
                .requestModeration(Objects.requireNonNullElse(newEventDto.getRequestModeration(), true))
                .state(EventState.PENDING)
                .build();

        // Сохраняем событие в репозитории
        Event savedEvent = eventRepository.save(event);

        // Преобразуем сохранённую модель в DTO
        EventDto savedEventDto = eventMapper.toDto(savedEvent);
        // Добавляем данные инициатора
        savedEventDto.setInitiator(userShortDto);
        log.info("Событие создано: {}", savedEventDto);
        return savedEventDto;
    }

    @Override
    public EventDto updateEventByUser(Long eventId, UpdateEventUserRequest newEventDto, Long userId) {
        Event event = findEventById(eventId);
        validateInitiator(event, userId);

        if (!List.of(EventState.CANCELED, EventState.PENDING).contains(event.getState())) {
            throw new ConflictException(EVENT_STATE_ACTION_CONFLICT_MESSAGE);
        }

        return updateEvent(event, eventMapper.toNewEventDto(newEventDto));
    }

    @Override
    public EventDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest newEventDto) {
        // Получаем событие по ID
        Event event = findEventById(eventId);

        // Проверяем и обрабатываем действие над состоянием события
        if (newEventDto.getStateAction() != null) {
            validateAdminStateAction(newEventDto.getStateAction(), event);
        }

        return updateEvent(event, eventMapper.toNewEventDto(newEventDto));
    }

    @Override
    public List<EventDto> findAllByParams(Long userId, Integer from, Integer size) {
        // Создание объекта PageRequest для пагинации и сортировки
        PageRequest pageRequest = createPageRequest(from, size);

        // Получаем список событий из репозитория
        List<Event> events = eventRepository.findAllByInitiatorId(userId, pageRequest)
                .stream()
                .toList();

        // Подготавливаем DTO событий с пользователями и статистикой
        return createEventDtoListWithAdditionalInfo(events);
    }

    @Override
    public EventDto findUserEvent(Long userId, Long eventId) {
        // Получаем событие по ID
        Event event = findEventById(eventId);

        // Проверяем, что событие принадлежит пользователю
        validateInitiator(event, userId);

        // Возвращаем DTO события
        return createEventDtoWithAdditionalInfo(event);
    }

    @Override
    public Event findEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(GET_EVENT_ERROR_MESSAGE, eventId));
    }

    @Override
    public List<EventDto> findAllByAdminParams(AdminEventParams adminEventParams) {
        // Проверяем корректность диапазона дат
        validateDateRange(adminEventParams.getRangeStart(), adminEventParams.getRangeEnd());

        // Создаём объект PageRequest
        PageRequest pageRequest = createPageRequest(
                adminEventParams.getFrom(),
                adminEventParams.getSize()
        );

        // Получение событий из репозитория по спецификации
        List<Event> events = eventRepository.findAll(withAdminParams(adminEventParams), pageRequest)
                .stream()
                .toList();

        // Подготавливаем DTO событий с пользователями и статистикой
        return createEventDtoListWithAdditionalInfo(events);
    }

    @Override
    public List<EventDto> findAllByUserParams(UserEventParams userEventParams) {
        log.debug("Получение событий с параметрами: {}", userEventParams);

        // Проверка корректности диапазона дат
        validateDateRange(userEventParams.getRangeStart(), userEventParams.getRangeEnd());

        // Создание объекта PageRequest для пагинации и сортировки
        PageRequest pageRequest = createPageRequest(
                userEventParams.getFrom(),
                userEventParams.getSize()
        );

        // Получение событий из репозитория по спецификации
        List<Event> events = eventRepository.findAll(withUserParams(userEventParams), pageRequest)
                .stream()
                .toList();

        // Получаем список DTO событий с пользователями и статистикой
        List<EventDto> eventDtos = createEventDtoListWithAdditionalInfo(events);

        // Фильтруем события по доступности (если требуется)
        if (Boolean.TRUE.equals(userEventParams.getOnlyAvailable())) {
            log.debug("Применена фильтрация по доступности");
            eventDtos = eventDtos.stream()
                    .filter(eventDto -> eventDto.getConfirmedRequests() < eventDto.getParticipantLimit())
                    .toList();
        }

        // Проверяем наличие и значение параметра сортировки
        if (userEventParams.getSort() != null
                && userEventParams.getSort().equals(EventSort.RATING)) {
            log.debug("Применена сортировка по рейтингу");
            eventDtos.sort(Comparator.comparing(EventDto::getRating).reversed());
        }

        // Возвращаем список DTO событий
        return eventDtos;
    }

    @Override
    public EventDto findPublishedEvent(Long eventId, Long userId) {
        Event event = findEventById(eventId);

        if (!EventState.PUBLISHED.equals(event.getState())) {
            throw new NotFoundException(String.format(EVENT_NOT_PUBLISHED_ERROR_MESSAGE, eventId));
        }
        // Проверяем, что пользователь с указанным ID существует
        getUserOrThrow(userId);
        // Отправляем действие пользователя в коллектор
        sendUserAction(userId, eventId, ActionTypeProto.ACTION_VIEW);
        // Возвращаем DTO события
        return createEventDtoWithAdditionalInfo(event);
    }

    @Override
    public EventDto findEventDtoById(Long eventId) {
        Event event = findEventById(eventId);
        return createEventDtoWithAdditionalInfo(event);
    }

    @Override
    public List<EventDto> findAllEventsByInitiatorId(Long initiatorId) {
        // Получение событий из репозитория
        List<Event> events = eventRepository.findAllByInitiatorId(initiatorId).stream().toList();
        // Возвращаем список DTO событий
        return createEventDtoListWithAdditionalInfo(events);
    }

    @Override
    public List<EventDto> getRecommendations(Long userId) {
        // Проверяем, что пользователь с указанным ID существует
        getUserOrThrow(userId);
        // Формируем запрос к сервису аналитики
        UserPredictionsRequest userPredictionsRequest = UserPredictionsRequest.newBuilder()
                .setUserId(userId)
                .build();
        // Получаем рекомендации для пользователя
        List<RecommendedEvent> recommendedEvents = analyzerClient.getRecommendationsForUser(userPredictionsRequest);
        // Если рекомендаций нет, возвращаем пустой список
        if (recommendedEvents.isEmpty()) {
            return List.of();
        }
        // Преобразовываем рекомендации в Map<Long, Double>
        Map<Long, Double> recommendations = recommendedEvents.stream()
                .collect(Collectors.toMap(
                        RecommendedEvent::getEventId,
                        RecommendedEvent::getScore
                ));
        // Получаем события по ID
        List<Event> events = eventRepository.findAllById(recommendations.keySet());
        // Создаем DTO событий с дополнительной информацией
        List<EventDto> eventDtos = createEventDtoListWithAdditionalInfo(events);
        // Сортируем DTO по релевантности и возвращаем результат
        return eventDtos.stream()
                .sorted((e1, e2) -> {
                    double s1 = recommendations.getOrDefault(e1.getId(), 0.0);
                    double s2 = recommendations.getOrDefault(e2.getId(), 0.0);
                    return Double.compare(s2, s1); // По убыванию
                })
                .toList();
    }

    @Override
    public void addLike(Long eventId, Long userId) {
        // Проверяем, что пользователь с указанным ID существует
        getUserOrThrow(userId);
        // Проверяем, что событие с указанным ID существует
        Event event = findEventById(eventId);
        // Проверяем, что событие находится в состоянии "Опубликовано"
        if (!EventState.PUBLISHED.equals(event.getState())) {
            throw new NotFoundException(String.format(EVENT_NOT_PUBLISHED_ERROR_MESSAGE, eventId));
        }
        // Отправляем действие пользователя в коллектор
        sendUserAction(userId, eventId, ActionTypeProto.ACTION_LIKE);
    }

    private void sendUserAction(Long userId, Long eventId, ActionTypeProto actionType) {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));
        long epochSecond = now.atOffset(ZoneOffset.UTC).toEpochSecond();

        UserActionProto userAction = UserActionProto.newBuilder()
                .setUserId(userId)
                .setEventId(eventId)
                .setActionType(actionType)
                .setTimestamp(Timestamp.newBuilder().setSeconds(epochSecond).build())
                .build();

        collectorClient.newUserAction(userAction);
        log.debug("В коллектор отправлено действие пользователя c ID={} с типом {} на событие c ID={}",
                userId, actionType, eventId);
    }

    private EventDto createEventDtoWithAdditionalInfo(Event event) {
        // Возвращаем DTO события
        return createEventDtoListWithAdditionalInfo(List.of(event)).getFirst();
    }

    private List<EventDto> createEventDtoListWithAdditionalInfo(List<Event> events) {
        if (events == null || events.isEmpty()) {
            return List.of(); // Нет событий — ничего не делать
        }
        // Получаем список уникальных ID инициаторов событий
        List<Long> initiatorIds = events.stream()
                .map(Event::getInitiatorId)
                .distinct()
                .toList();
        // Получаем пользователей по ID
        Map<Long, UserShortDto> users = getUsersOrThrow(initiatorIds);

        // Получаем список уникальных ID событий
        List<Long> eventsIds = events.stream()
                .map(Event::getId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        // Запрашиваем количество подтверждённых заявок
        Map<Long, Long> confirmedRequestsCount = loadConfirmedRequestsCount(eventsIds);
        // Запрашиваем рейтинги событий
        Map<Long, Double> ratings = loadRatings(eventsIds);

        // Возвращаем список DTO событий c с дополнительной информацией
        return events.stream()
                .map(event -> {
                    EventDto dto = eventMapper.toDto(event);
                    dto.setInitiator(users.get(event.getInitiatorId()));
                    dto.setConfirmedRequests(confirmedRequestsCount.getOrDefault(event.getId(), 0L));
                    dto.setRating(ratings.getOrDefault(event.getId(), 0.0));
                    return dto;
                })
                .toList();
    }

    private Map<Long, Double> loadRatings(List<Long> eventIds) {
        log.debug("Загрузка рейтингов для событий: {}", eventIds);
        // Создание запроса на получение рейтинга
        InteractionsCountRequest request = InteractionsCountRequest.newBuilder()
                .addAllEventIds(eventIds)
                .build();

        // Получение данных из сервиса analyzerClient
        List<RecommendedEvent> recommendedEvents = analyzerClient.getInteractionsCount(request);

        // Преобразование списка в Map<Long, Double>
        return recommendedEvents.stream()
                .collect(Collectors.toMap(
                        RecommendedEvent::getEventId,
                        RecommendedEvent::getScore
                ));
    }

    private Map<Long, Long> loadConfirmedRequestsCount(List<Long> eventIds) {
        log.debug("Загрузка количества подтверждённых заявок для событий: {}", eventIds);

        try {
            ResponseEntity<List<EventRequestsCountDto>> response = requestClient.getEventRequestsCount(eventIds);

            // Проверка успешности запроса и наличия данных
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.warn("Неуспешный ответ от сервиса заявок: HTTP {}", response.getStatusCode());
                return Collections.emptyMap();
            }

            if (response.getBody() == null) {
                log.warn("Ответ от сервиса заявок не содержит тело для событий: {}", eventIds);
                return Collections.emptyMap();
            }

            // Преобразование данных в маппинг
            return response.getBody().stream()
                    .collect(Collectors.toMap(
                            EventRequestsCountDto::getEventId,
                            EventRequestsCountDto::getConfirmedRequests
                    ));

        } catch (FeignException fe) {
            log.error("Ошибка при получении количества подтверждённых заявок для событий {}: {}",
                    eventIds, fe.getMessage(), fe);
            return Collections.emptyMap();
        }
    }

    private void validateInitiator(Event event, Long userId) {
        if (!event.getInitiatorId().equals(userId)) {
            throw new ConflictException(String.format(EVENT_NOT_OWNED_BY_USER_ERROR_MESSAGE, event.getId(), userId));
        }
    }

    private void validateAdminStateAction(EventStateAction action, Event event) {
        switch (action) {
            case PUBLISH_EVENT:
                if (!EventState.PENDING.equals(event.getState())) {
                    throw new ConflictException(PUBLISH_NOT_PENDING_EVENT_ERROR_MESSAGE);
                }
                validatePublishDate(event);
                break;
            case REJECT_EVENT:
                if (EventState.PUBLISHED.equals(event.getState())) {
                    throw new ConflictException(REJECT_PUBLISHED_EVENT_ERROR_MESSAGE);
                }
                break;
            default:
                throw new ConflictException(UNACCEPTABLE_ACTION_ON_EVENT_ERROR_MESSAGE, action);
        }
    }

    private void validatePublishDate(Event event) {
        LocalDateTime nowPlusHour = LocalDateTime.now().plusHours(1L);
        if (nowPlusHour.isAfter(event.getEventDate())) {
            throw new ConflictException(EARLY_START_ERROR_MESSAGE);
        }
    }

    private EventDto updateEvent(Event event, NewEventDto request) {
        if (EventStateAction.PUBLISH_EVENT.equals(request.getStateAction())) {
            LocalDateTime nowPlusHour = LocalDateTime.now().plusHours(1L);
            LocalDateTime eventDate = request.getEventDate() != null ? request.getEventDate() : event.getEventDate();

            if (nowPlusHour.isAfter(eventDate)) {
                throw new ConflictException(EARLY_START_ERROR_MESSAGE);
            }
        }
        // Обновляем поля события
        updateEventFields(event, request);
        // Сохраняем изменения в репозитории
        eventRepository.save(event);
        log.info("Событие изменено: {}", event);
        // Возвращаем DTO события
        return createEventDtoWithAdditionalInfo(event);
    }

    private UserShortDto getUserOrThrow(long userId) {
        try {
            ResponseEntity<UserShortDto> response = userClient.getUser(userId);

            // Проверка наличия тела ответа
            if (response.getStatusCode().is2xxSuccessful() && response.hasBody()) {
                return response.getBody();
            }

            // Если статус успешный, но тело отсутствует
            throw new NotFoundException(String.format(USER_NOT_FOUND_ERROR_MESSAGE, userId));

        } catch (FeignException fe) {
            log.error("Ошибка запроса к пользовательскому сервису: {} [HTTP {}: {}]",
                    userId, fe.status(), fe.getMessage(), fe);
            throw new NotFoundException(String.format(USER_NOT_FOUND_ERROR_MESSAGE, userId), fe);
        }
    }

    private Map<Long, UserShortDto> getUsersOrThrow(List<Long> userIds) {
        try {
            ResponseEntity<List<UserShortDto>> response = userClient.getUsers(userIds);

            // Проверка успешного статуса ответа
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.warn("Пользовательский сервис вернул статус {}: {}", response.getStatusCode(), response.getHeaders());
                throw new NotFoundException(String.format(USERS_NOT_FOUND_ERROR_MESSAGE, userIds));
            }

            // Проверка наличия тела ответа
            List<UserShortDto> users = response.getBody();
            if (users == null || users.isEmpty()) {
                log.warn("Ответ от пользовательского сервиса пуст для пользователей {}", userIds);
                throw new NotFoundException(String.format(USERS_NOT_FOUND_ERROR_MESSAGE, userIds));
            }

            return users.stream()
                    .collect(Collectors.toMap(
                            UserShortDto::getId,
                            user -> user,
                            (existing, replacement) -> existing // Обработка дубликатов (ожидается, что их нет)
                    ));

        } catch (FeignException fe) {
            log.error("Ошибка при получении пользователей {}: HTTP {} - {}",
                    userIds, fe.status(), fe.getMessage(), fe);
            throw new NotFoundException(String.format(USERS_NOT_FOUND_ERROR_MESSAGE, userIds), fe);
        }
    }

    private void updateEventFields(Event event, NewEventDto request) {
        if (request.getEventDate() != null) {
            event.setEventDate(request.getEventDate());
        }
        if (request.getAnnotation() != null) {
            event.setAnnotation(request.getAnnotation());
        }
        if (request.getCategory() != null) {
            event.setCategory(categoryService.getCategoryById(request.getCategory()));
        }
        if (request.getDescription() != null) {
            event.setDescription(request.getDescription());
        }
        if (request.getLocation() != null) {
            event.setLocationLat(request.getLocation().getLat());
            event.setLocationLon(request.getLocation().getLon());
        }
        if (request.getPaid() != null) {
            event.setPaid(request.getPaid());
        }
        if (request.getParticipantLimit() != null) {
            event.setParticipantLimit(request.getParticipantLimit());
        }
        if (request.getRequestModeration() != null) {
            event.setRequestModeration(request.getRequestModeration());
        }
        if (request.getTitle() != null) {
            event.setTitle(request.getTitle());
        }
        if (request.getStateAction() != null) {
            switch (request.getStateAction()) {
                case REJECT_EVENT, CANCEL_REVIEW -> event.setState(EventState.CANCELED);
                case PUBLISH_EVENT -> {
                    event.setState(EventState.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                }
                case SEND_TO_REVIEW -> event.setState(EventState.PENDING);
            }
        }
    }

    private PageRequest createPageRequest(int from, int size) {
        return PageRequest.of(
                from / size,
                size,
                Sort.by(EVENT_DATE.getSortField()).ascending()
        );
    }

    private void validateDateRange(LocalDateTime start, LocalDateTime end) {
        if (start != null && end != null && !end.isAfter(start)) {
            throw new ValidationException(RANGE_ERROR_MESSAGE);
        }
    }
}
