package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.api.event.AdminEventApi;
import ru.practicum.category_service.dto.event.EventFullDto;
import ru.practicum.category_service.dto.event.UpdateEventAdminRequest;
import ru.practicum.service.EventService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class AdminEventController implements AdminEventApi {
    private final EventService eventService;

    @Override
    public List<EventFullDto> getEvents(
            List<Long> users,
            List<String> states,
            List<Long> categories,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            int from,
            int size
    ) {
        return eventService.getEventsForAdmin(users, states, categories, rangeStart, rangeEnd,
                PageRequest.of(from / size, size));
    }

    @Override
    public EventFullDto updateEvent(Long eventId, UpdateEventAdminRequest updateRequest) {
        return eventService.updateEventByAdmin(eventId, updateRequest);
    }
}
