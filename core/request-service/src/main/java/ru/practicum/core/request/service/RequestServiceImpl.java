package ru.practicum.core.request.service;

import com.google.protobuf.Timestamp;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.core.api.exception.ConflictException;
import ru.practicum.core.api.exception.NotFoundException;
import ru.practicum.core.api.internal.event.client.EventClient;
import ru.practicum.core.api.internal.event.dto.EventDto;
import ru.practicum.core.api.internal.request.dto.EventRequestsCountDto;
import ru.practicum.core.api.internal.user.client.UserClient;
import ru.practicum.core.api.internal.user.dto.UserShortDto;
import ru.practicum.core.api.util.enums.EventState;
import ru.practicum.core.api.util.enums.RequestStatus;
import ru.practicum.core.request.dto.ChangeRequestStatusDto;
import ru.practicum.core.request.dto.ParticipationRequestDto;
import ru.practicum.core.request.dto.UserParticipationRequestDto;
import ru.practicum.core.request.mapper.EventRequestsCountMapper;
import ru.practicum.core.request.mapper.ParticipationRequestMapper;
import ru.practicum.core.request.model.ParticipationRequest;
import ru.practicum.core.request.repository.RequestRepository;
import ru.practicum.recomm.client.CollectorClient;
import ru.practicum.recommendations.messages.ActionTypeProto;
import ru.practicum.recommendations.messages.UserActionProto;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.core.api.exception.NotFoundException.notFoundException;

@Slf4j
@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {
    private static final String USER_NOT_FOUND_MESSAGE = "Пользователь с идентификатором %d не найден!";
    private static final String EVENT_NOT_FOUND_MESSAGE = "Событие с идентификатором %d не найдено!";
    private static final String EVENT_FULL_MESSAGE = "Событие %d полностью забронировано";
    private static final String RETURNED_STATUS_MESSAGE = "Пользовательский сервис вернул статус {}: {}";

    private final RequestRepository requestRepository;
    private final ParticipationRequestMapper participationRequestMapper;
    private final EventRequestsCountMapper eventRequestsCountMapper;

    private final UserClient userClient;
    private final EventClient eventClient;
    private final CollectorClient collectorClient;

    @Override
    public List<ParticipationRequestDto> getAllRequestsByUser(Long requesterId) {
        return requestRepository.findByRequesterId(requesterId)
                .stream()
                .map(participationRequestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        validateUserExists(userId);
        EventDto eventDto = getPublishedEventOrThrow(eventId);

        validateUserNotInitiator(userId, eventDto);
        validateNoDuplicateRequest(userId, eventDto);
        validateEventAvailability(eventDto);
        validateRequestCapacity(eventDto);

        ParticipationRequest request = ParticipationRequest.builder()
                .requesterId(userId)
                .eventId(eventId)
                .status(calculateInitialRequestStatus(eventDto))
                .created(LocalDateTime.now())
                .build();

        ParticipationRequest saved = requestRepository.save(request);
        log.info("Создана заявка {} от пользователя {} на событие {}", saved.getId(), userId, eventId);
        // Отправляем действие пользователя в коллектор
        sendUserAction(userId, eventId, ActionTypeProto.ACTION_REGISTER);
        return participationRequestMapper.toDto(saved);
    }

    @Override
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        ParticipationRequest request = requestRepository.findByIdAndRequesterId(requestId, userId)
                .orElseThrow(notFoundException("Заявка {0} пользователя {1} не найдена", requestId, userId));

        request.setStatus(RequestStatus.CANCELED);
        ParticipationRequest updated = requestRepository.save(request);
        log.info("Отменена заявка {} пользователя {}", requestId, userId);
        return participationRequestMapper.toDto(updated);
    }

    @Override
    public List<ParticipationRequestDto> getUserRequestsForEvent(Long userId, Long eventId) {
        validateUserExists(userId);
        return requestRepository.findAllByEventId(eventId)
                .stream()
                .map(participationRequestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserParticipationRequestDto updateRequestStatus(ChangeRequestStatusDto dto,
                                                           Long userId,
                                                           Long eventId) {
        EventDto eventDto = getPublishedEventOrThrow(eventId);
        List<ParticipationRequest> requests = validateAndFetchRequests(dto.getRequestIds(), eventId);
        validateRequestCapacity(eventDto);
        processRequests(dto.getStatus(), requests);
        return mapToResponse(requests);
    }

    @Override
    public List<EventRequestsCountDto> getEventRequestsCount(List<Long> eventIds) {
        return requestRepository.countByStatusForEvents(eventIds, RequestStatus.CONFIRMED).stream()
                .map(eventRequestsCountMapper::toDto)
                .toList();
    }

    @Override
    public Boolean hasRequest(Long userId, Long eventId) {
        return requestRepository.existsByRequesterIdAndEventId(userId, eventId);
    }

    private void validateUserExists(Long userId) {
        try {
            ResponseEntity<UserShortDto> response = userClient.getUser(userId);

            // Проверка успешного статуса ответа и наличия тела
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.warn(RETURNED_STATUS_MESSAGE, response.getStatusCode(), response.getHeaders());
                throw new NotFoundException(String.format(USER_NOT_FOUND_MESSAGE, userId));
            }

            if (response.getBody() == null) {
                log.warn("Ответ от пользовательского сервиса не содержит тело для пользователя ID={}", userId);
                throw new NotFoundException(String.format(USER_NOT_FOUND_MESSAGE, userId));
            }

        } catch (FeignException fe) {
            log.error("Ошибка при проверке существования пользователя ID={}: HTTP {} - {}",
                    userId, fe.status(), fe.getMessage(), fe);
            throw new NotFoundException(String.format(USER_NOT_FOUND_MESSAGE, userId), fe);
        }
    }

    private EventDto getPublishedEventOrThrow(Long eventId) {
        try {
            ResponseEntity<EventDto> response = eventClient.getEventById(eventId);

            // Проверка успешного статуса ответа и наличия тела
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.warn(RETURNED_STATUS_MESSAGE, response.getStatusCode(), response.getHeaders());
                throw new NotFoundException(String.format(EVENT_NOT_FOUND_MESSAGE, eventId));
            }

            if (response.getBody() == null) {
                log.warn("Ответ от пользовательского сервиса не содержит тело для события ID={}", eventId);
                throw new NotFoundException(String.format(EVENT_NOT_FOUND_MESSAGE, eventId));
            }

            return response.getBody();

        } catch (FeignException fe) {
            log.error("Ошибка при проверке существования события ID={}: HTTP {} - {}",
                    eventId, fe.status(), fe.getMessage(), fe);
            throw new NotFoundException(String.format(EVENT_NOT_FOUND_MESSAGE, eventId), fe);
        }
    }

    private void validateUserNotInitiator(Long userId, EventDto eventShortDto) {
        if (userId.equals(eventShortDto.getInitiator().getId())) {
            throw new ConflictException("Инициатор события не может подавать заявки");
        }
    }

    private void validateNoDuplicateRequest(Long userId, EventDto eventShortDto) {
        if (requestRepository.existsByRequesterIdAndEventId(userId, eventShortDto.getId())) {
            throw new ConflictException("Пользователь уже подал заявку на это событие");
        }
    }

    private void validateEventAvailability(EventDto eventDto) {
        if (!EventState.PUBLISHED.equals(eventDto.getState())) {
            throw new ConflictException("Нельзя участвовать в неопубликованном событии");
        }
    }

    private RequestStatus calculateInitialRequestStatus(EventDto eventDto) {
        return eventDto.getRequestModeration() && eventDto.getParticipantLimit() > 0
                ? RequestStatus.PENDING
                : RequestStatus.CONFIRMED;
    }

    private List<ParticipationRequest> validateAndFetchRequests(List<Long> requestIds, Long eventId) {
        List<ParticipationRequest> requests = requestRepository.findAllById(requestIds);
        if (requests.size() != requestIds.size()) {
            throw new NotFoundException("Не все заявки найдены");
        }

        if (!requests.stream().allMatch(r -> r.getEventId().equals(eventId))) {
            throw new ConflictException("Заявки принадлежат разным событиям");
        }

        if (!requests.stream().allMatch(r -> r.getStatus() == RequestStatus.PENDING)) {
            throw new ConflictException("Найдены заявки, отличные от состояния ОЖИДАНИЕ");
        }

        return requests;
    }

    private void validateRequestCapacity(EventDto eventDto) {
        if (eventDto.getParticipantLimit() > 0) {
            long confirmedCount = requestRepository.countByEventIdAndStatus(eventDto.getId(), RequestStatus.CONFIRMED);
            int remainingSlots = eventDto.getParticipantLimit() - (int) confirmedCount;

            if (remainingSlots <= 0) {
                throw new ConflictException(String.format(EVENT_FULL_MESSAGE, eventDto.getId()));
            }
        }
    }

    private void processRequests(RequestStatus targetStatus, List<ParticipationRequest> requests) {
        for (ParticipationRequest request : requests) {
            request.setStatus(targetStatus);
        }
        requestRepository.saveAll(requests);
    }

    private UserParticipationRequestDto mapToResponse(List<ParticipationRequest> requests) {
        return UserParticipationRequestDto.builder()
                .confirmedRequests(requests.stream()
                        .filter(r -> r.getStatus() == RequestStatus.CONFIRMED)
                        .map(participationRequestMapper::toDto)
                        .collect(Collectors.toList()))
                .rejectedRequests(requests.stream()
                        .filter(r -> r.getStatus() == RequestStatus.REJECTED)
                        .map(participationRequestMapper::toDto)
                        .collect(Collectors.toList()))
                .build();
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
}