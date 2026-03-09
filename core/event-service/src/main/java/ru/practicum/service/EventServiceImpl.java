package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.StatResponseDto;
import ru.practicum.StatsClient;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.UpdateEventAdminRequest;
import ru.practicum.dto.enums.EventState;
import ru.practicum.dto.enums.RequestStatus;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.feignClient.CategoryServiceClient;
import ru.practicum.feignClient.ParticipationRequestServiceClient;
import ru.practicum.feignClient.UserServiceClient;
import ru.practicum.mapper.EventMapper;
import ru.practicum.model.Event;
import ru.practicum.repository.EventRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    private final ParticipationRequestServiceClient participationRequestClient;
    private final CategoryServiceClient categoryClient;
    private final UserServiceClient userClient;

    private final StatsClient statsClient;

    @Override
    public List<EventFullDto> getEventsForAdmin(List<Long> users, List<String> states,
                                                List<Long> categories, LocalDateTime rangeStart,
                                                LocalDateTime rangeEnd, Pageable pageable) {
        Specification<Event> spec = Specification.where(null);
        if (users != null && !users.isEmpty()) {
            spec = spec.and(((root, query, criteriaBuilder) ->
                    root.get("initiatorId").in(users)));
        }
        if (states != null && !states.isEmpty()) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    root.get("state").as(String.class).in(states));
        }
        if (categories != null && !categories.isEmpty()) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    root.get("categoryId").in(categories));
        }
        if (rangeEnd != null) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.lessThanOrEqualTo(root.get("eventDate"), rangeEnd));
        }
        if (rangeStart != null) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.greaterThanOrEqualTo(root.get("eventDate"), rangeStart));
        }
        List<Event> events = eventRepository.findAll(spec, pageable).toList();
        Map<Long, Long> views = getEventsViews(events);
        Map<Long, List<ParticipationRequestDto>> confRequests = participationRequestClient.getConfirmedRequestsCount(
                events.stream().map(Event::getId).toList(),
                RequestStatus.CONFIRMED
        );
        return events.stream()
                .map(e -> eventMapper.toEventFullDtoWithDetails(
                        e,
                        categoryClient.getCategoryById(e.getCategoryId()),
                        userClient.getUserShortDtoById(e.getInitiatorId())
                ))
                .peek(dto -> dto.setViews(views.getOrDefault(dto.getId(), 0L)))
                .peek(dto -> dto.setConfirmedRequests((confRequests.getOrDefault(dto.getId(), List.of())).size()))
                .toList();

    }


    @Override
    @Transactional
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));
        UserShortDto userShortDto = userClient.getUserShortDtoById(event.getInitiatorId());
        CategoryDto categoryDto = categoryClient.getCategoryById(event.getCategoryId());

        if (updateRequest.getStateAction() != null) {
            if (updateRequest.getStateAction().equals("PUBLISH_EVENT")) {
                if (event.getState() != EventState.PENDING) {
                    throw new ConflictException("Cannot publish event that is not in PENDING state");
                }
                if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
                    throw new ConflictException("Event date must be at least 1 hour from publication");
                }
                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            } else if (updateRequest.getStateAction().equals("REJECT_EVENT")) {
                if (event.getState() == EventState.PUBLISHED) {
                    throw new ConflictException("Cannot reject published event");
                }
                event.setState(EventState.CANCELED);
            }
        }

        updateEventFields(event, updateRequest);
        Event updatedEvent = eventRepository.save(event);
        return eventMapper.toEventFullDtoWithDetails(updatedEvent, categoryDto, userShortDto);
    }

    private void updateEventFields(Event event, UpdateEventAdminRequest updateRequest) {
        if (updateRequest.getAnnotation() != null) {
            event.setAnnotation(updateRequest.getAnnotation());
        }
        if (updateRequest.getCategory() != null) {
            CategoryDto category = categoryClient.getCategoryById(updateRequest.getCategory());
            event.setCategoryId(category.getId());
        }
        if (updateRequest.getDescription() != null) {
            event.setDescription(updateRequest.getDescription());
        }
        if (updateRequest.getEventDate() != null) {
            event.setEventDate(updateRequest.getEventDate());
        }
        if (updateRequest.getLocation() != null) {
            ru.practicum.model.Location location =
                    eventMapper.toLocation(updateRequest.getLocation());
            event.setLocation(location);
        }
        if (updateRequest.getPaid() != null) {
            event.setPaid(updateRequest.getPaid());
        }
        if (updateRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateRequest.getParticipantLimit());
        }
        if (updateRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateRequest.getRequestModeration());
        }
        if (updateRequest.getTitle() != null) {
            event.setTitle(updateRequest.getTitle());
        }
    }

    private Map<Long, Long> getEventsViews(List<Event> events) {
        List<String> uris = events
                .stream()
                .map(event -> String.format("/events/%s", event.getId()))
                .toList();
        LocalDateTime startDate = events
                .stream()
                .map(Event::getCreatedOn)
                .min(LocalDateTime::compareTo)
                .orElse(null);
        Map<Long, Long> viewStats = new HashMap<>();
        if (startDate != null) {
            List<StatResponseDto> stats = statsClient.getStats(startDate, LocalDateTime.now(),
                    uris, true);
            viewStats = stats
                    .stream()
                    .filter(s -> s.getUri().startsWith("/events/"))
                    .collect(Collectors.toMap(s -> Long.parseLong(s.getUri().substring("/events/".length())),
                            StatResponseDto::getHits));
        }
        return viewStats;
    }
}