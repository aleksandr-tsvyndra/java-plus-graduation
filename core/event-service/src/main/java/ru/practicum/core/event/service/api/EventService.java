package ru.practicum.core.event.service.api;

import ru.practicum.core.event.dto.events.AdminEventParams;
import ru.practicum.core.api.internal.event.dto.EventDto;
import ru.practicum.core.event.dto.events.NewEventDto;
import ru.practicum.core.event.dto.events.UpdateEventAdminRequest;
import ru.practicum.core.event.dto.events.UpdateEventUserRequest;
import ru.practicum.core.event.dto.events.UserEventParams;
import ru.practicum.core.event.model.Event;

import java.util.List;

public interface EventService {

    EventDto addEvent(NewEventDto newEventDto, Long userId);

    EventDto updateEventByUser(Long eventId, UpdateEventUserRequest newEventDto, Long userId);

    EventDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest newEventDto);

    List<EventDto> findAllByParams(Long userId, Integer from, Integer size);

    EventDto findUserEvent(Long userId, Long eventId);

    Event findEventById(Long eventId);

    List<EventDto> findAllByAdminParams(AdminEventParams adminEventParams);

    List<EventDto> findAllByUserParams(UserEventParams userEventParams);

    EventDto findPublishedEvent(Long eventId, Long userId);

    EventDto findEventDtoById(Long eventId);

    List<EventDto> findAllEventsByInitiatorId(Long initiatorId);

    List<EventDto> getRecommendations(Long userId);

    void addLike(Long eventId, Long userId);
}