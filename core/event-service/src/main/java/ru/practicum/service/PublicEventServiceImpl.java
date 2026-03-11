package ru.practicum.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.EndpointHitDto;
import ru.practicum.StatResponseDto;
import ru.practicum.StatsClient;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.enums.EventState;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.feignClient.CategoryServiceClient;
import ru.practicum.feignClient.UserServiceClient;
import ru.practicum.mapper.EventMapper;
import ru.practicum.model.Event;
import ru.practicum.repository.EventRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicEventServiceImpl implements PublicEventService {
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    private final UserServiceClient userClient;
    private final CategoryServiceClient categoryClient;

    private final StatsClient statsClient;

    @Override
    public List<EventShortDto> getEventsPublic(String text, List<Long> categories, Boolean paid,
                                               LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                               Boolean onlyAvailable, String sort, Pageable pageable,
                                               HttpServletRequest request) {
        verifyRange(rangeStart, rangeEnd);
        statsClient.hit(EndpointHitDto
                .builder()
                .app("ewm-service")
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now())
                .build());
        Specification<Event> spec = Specification.where(null);
        if (text != null && !text.isBlank()) spec = spec.and(searchText(text.toLowerCase()));
        if (categories != null && !categories.isEmpty()) spec = spec.and(searchCategoryIn(categories));
        spec = spec.and(searchAfterDate(rangeStart));
        if (rangeEnd != null) spec = spec.and(searchBeforeDate(rangeEnd));
        if (onlyAvailable) spec = spec.and(searchAvailable());
        spec = spec.and(searchPublished());
        List<Event> results = eventRepository.findAll(spec, pageable).toList();
        Map<Long, Long> views = getEventsViews(results);
        return results
                .stream()
                .map(e -> eventMapper.toEventShortDtoWithDetails(
                        e,
                        categoryClient.getCategoryById(e.getCategoryId()),
                        userClient.getUserShortDtoById(e.getInitiatorId())))
                .peek(dto -> dto.setViews(views.getOrDefault(dto.getId(), 0L)))
                .toList();
    }

    @Override
    @Transactional
    public EventFullDto getEventById(Long id, HttpServletRequest request) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Event not found"));
        UserShortDto userShortDto = userClient.getUserShortDtoById(event.getInitiatorId());
        CategoryDto categoryDto = categoryClient.getCategoryById(event.getCategoryId());
        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException("Event not found");
        }
        statsClient.hit(EndpointHitDto
                .builder()
                .app("ewm-service")
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now())
                .build());
        EventFullDto eventDto = eventMapper.toEventFullDtoWithDetails(event, categoryDto, userShortDto);
        Map<Long, Long> views = getEventsViews(List.of(event));
        eventDto.setViews(views.getOrDefault(id, 0L));
        return eventDto;
    }

    @Override
    public void validateCategoryHasNoEvents(Long categoryId) {
        if (eventRepository.existsByCategoryId(categoryId)) {
            throw new ConflictException("Category has associated events and cannot be deleted");
        }
    }

    @Override
    public void validateEventExistingById(Long id) {
        if (eventRepository.existsById(id)) {
            throw new NotFoundException("Event not found");
        }
    }

    @Override
    public Set<EventShortDto> getEventShortDtoSetByIds(Set<Long> eventIds) {
        return eventRepository.findAllByIdIn(eventIds)
                .stream()
                .map(event -> eventMapper.toEventShortDtoWithDetails(
                            event,
                            categoryClient.getCategoryById(event.getCategoryId()),
                            userClient.getUserShortDtoById(event.getInitiatorId()))
                )
                .collect(Collectors.toSet());
    }

    @Override
    public EventShortDto getEventShortDtoByIdClient(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Event with the same id not found"));
        UserShortDto userShortDto = userClient.getUserShortDtoById(event.getInitiatorId());
        CategoryDto categoryDto = categoryClient.getCategoryById(event.getCategoryId());
        return eventMapper.toEventShortDtoWithDetails(event, categoryDto, userShortDto);
    }

    @Override
    public EventFullDto getEventFullDtoByIdClient(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Event with the same id not found"));
        UserShortDto userShortDto = userClient.getUserShortDtoById(event.getInitiatorId());
        CategoryDto categoryDto = categoryClient.getCategoryById(event.getCategoryId());
        return eventMapper.toEventFullDtoWithDetails(event, categoryDto, userShortDto);
    }

    private Map<Long, Long> getEventsViews(List<Event> events) {
        List<String> uris = events
                .stream()
                .map(event -> String.format("/events/%s", event.getId()))
                .toList();
        LocalDateTime startDate = events
                .stream()
                .map(Event::getPublishedOn)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now().minusYears(1));
        Map<Long, Long> viewStats = new HashMap<>();
        try {
            LocalDateTime endDate = LocalDateTime.now();
            List<StatResponseDto> stats = statsClient.getStats(startDate, endDate,
                    uris, true);
            viewStats = stats
                    .stream()
                    .filter(s -> s.getUri().startsWith("/events/"))
                    .collect(Collectors.toMap(
                            s -> Long.parseLong(s.getUri().substring("/events/".length())),
                            StatResponseDto::getHits
                    ));
        } catch (Exception e) {
            log.error("Error getting stats: {}", e.getMessage());
        }
        return viewStats;
    }

    private void verifyRange(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        if (rangeStart != null && rangeEnd != null) {
            if (rangeStart.isAfter(rangeEnd)) {
                throw new BadRequestException("Дата начала ивента не может быть позже даты окончания");
            }
        }
    }

    private Specification<Event> searchText(String text) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("annotation")), "%" + text + "%"),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), "%" + text + "%")
                );
    }

    private Specification<Event> searchCategoryIn(List<Long> categories) {
        return (root, query, criteriaBuilder) ->
                root.get("categoryId").in(categories);
    }

    private Specification<Event> searchAfterDate(LocalDateTime rangeStart) {
        LocalDateTime start = Objects.requireNonNullElse(rangeStart, LocalDateTime.now());
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThan(root.get("eventDate"), start);
    }

    private Specification<Event> searchBeforeDate(LocalDateTime rangeEnd) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThan(root.get("eventDate"), rangeEnd);
    }

    private Specification<Event> searchAvailable() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(root.get("participantLimit"), 0);
    }

    private Specification<Event> searchPublished() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("state"), EventState.PUBLISHED);
    }
}