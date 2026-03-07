package ru.practicum.controller.privates;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import ru.practicum.api.event.PrivateEventApi;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.dto.event.UpdateEventUserRequest;
import ru.practicum.service.event.PrivateEventService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PrivateEventController implements PrivateEventApi {
    private final PrivateEventService privateEventService;

    @Override
    public List<EventShortDto> getEventsByUser(Long userId, int from, int size) {
        return privateEventService.getEventsByUser(userId, PageRequest.of(from / size, size));
    }

    @Override
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        return privateEventService.createEvent(userId, newEventDto);
    }

    @Override
    public EventFullDto getEventByUser(Long userId, Long eventId) {
        return privateEventService.getEventByUser(userId, eventId);
    }

    @Override
    public EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserRequest updateRequest) {
        return privateEventService.updateEventByUser(userId, eventId, updateRequest);
    }
}