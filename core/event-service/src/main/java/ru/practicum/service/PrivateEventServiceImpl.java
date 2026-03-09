package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.dto.event.UpdateEventUserRequest;
import ru.practicum.dto.enums.EventState;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.feignClient.CategoryServiceClient;
import ru.practicum.feignClient.UserServiceClient;
import ru.practicum.mapper.EventMapper;
import ru.practicum.model.Event;
import ru.practicum.model.Location;
import ru.practicum.repository.EventRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PrivateEventServiceImpl implements PrivateEventService {
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    private final UserServiceClient userClient;
    private final CategoryServiceClient categoryClient;


    @Override
    public List<EventShortDto> getEventsByUser(Long userId, Pageable pageable) {
        List<Event> events = eventRepository.findByInitiatorId(userId, pageable).getContent();
        return events.stream().map(event -> eventMapper.toEventShortDtoWithDetails(
                event,
                categoryClient.getCategoryById(event.getCategoryId()),
                userClient.getUserShortDtoById(event.getInitiatorId())))
                .toList();
    }


    @Override
    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        UserShortDto userShortDto = userClient.getUserShortDtoById(userId);
        CategoryDto categoryDto = categoryClient.getCategoryById(newEventDto.getCategory());
        if (newEventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new BadRequestException("До даты мероприятия должно быть не менее 2 часов");
        }
        Event event = eventMapper.toEvent(newEventDto);
        event.setInitiatorId(userShortDto.getId());
        event.setConfirmedRequests(0);
        event.setCategoryId(categoryDto.getId());
        event.setCreatedOn(LocalDateTime.now());
        event.setState(EventState.PENDING);
        if (newEventDto.getLocation() != null) {
            Location location =
                    eventMapper.toLocation(newEventDto.getLocation());
            event.setLocation(location);
        }
        Event savedEvent = eventRepository.save(event);
        return eventMapper.toEventFullDtoWithDetails(savedEvent, categoryDto, userShortDto);
    }

    @Override
    public EventFullDto getEventByUser(Long userId, Long eventId) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event not found"));
        UserShortDto userShortDto = userClient.getUserShortDtoById(userId);
        CategoryDto categoryDto = categoryClient.getCategoryById(event.getCategoryId());
        return eventMapper.toEventFullDtoWithDetails(event, categoryDto, userShortDto);
    }


    @Override
    @Transactional
    public EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserRequest updateRequest) {
        UserShortDto userShortDto = userClient.getUserShortDtoById(userId);
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event not found"));
        CategoryDto categoryDto = categoryClient.getCategoryById(event.getCategoryId());
        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Cannot update published event");
        }
        if (updateRequest.getEventDate() != null &&
                updateRequest.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new BadRequestException("Event date must be at least 2 hours from now");
        }
        if (updateRequest.getStateAction() != null) {
            if (updateRequest.getStateAction().equals("SEND_TO_REVIEW")) {
                event.setState(EventState.PENDING);
            } else if (updateRequest.getStateAction().equals("CANCEL_REVIEW")) {
                event.setState(EventState.CANCELED);
            }
        }
        updateEventFields(event, updateRequest);
        Event updatedEvent = eventRepository.save(event);
        return eventMapper.toEventFullDtoWithDetails(updatedEvent, categoryDto, userShortDto);
    }

    private void updateEventFields(Event event, UpdateEventUserRequest updateRequest) {
        if (updateRequest.getAnnotation() != null) {
            event.setAnnotation(updateRequest.getAnnotation());
        }
        if (updateRequest.getCategory() != null) {
            event.setCategoryId(updateRequest.getCategory());
        }
        if (updateRequest.getDescription() != null) {
            event.setDescription(updateRequest.getDescription());
        }
        if (updateRequest.getEventDate() != null) {
            event.setEventDate(updateRequest.getEventDate());
        }
        if (updateRequest.getLocation() != null) {
            event.setLocation(eventMapper.toLocation(updateRequest.getLocation()));
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
}