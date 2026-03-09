package ru.practicum.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import ru.practicum.api.event.PublicEventApi;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.service.PublicEventService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
public class PublicEventController implements PublicEventApi {
    private final PublicEventService publicEventService;

    @Override
    public List<EventShortDto> getEvents(
            String text,
            List<Long> categories,
            Boolean paid,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            Boolean onlyAvailable,
            String sort,
            int from,
            int size,
            HttpServletRequest httpRequest
    ) {
        return publicEventService.getEventsPublic(text, categories, paid, rangeStart, rangeEnd,
                onlyAvailable, sort, PageRequest.of(from / size, size), httpRequest);
    }

    @Override
    public EventFullDto getEventById(Long id, HttpServletRequest httpRequest) {
        return publicEventService.getEventById(id, httpRequest);
    }

    @Override
    public void validateCategoryHasNoEvents(Long categoryId) {
        publicEventService.validateCategoryHasNoEvents(categoryId);
    }

    @Override
    public void validateEventExistingById(Long eventId) {
        publicEventService.validateEventExistingById(eventId);
    }

    @Override
    public Set<EventShortDto> getEventShortDtoSetByIds(Set<Long> eventIds) {
        return publicEventService.getEventShortDtoSetByIds(eventIds);
    }

    @Override
    public EventShortDto getEventShortDtoByIdClient(Long id) {
        return publicEventService.getEventShortDtoByIdClient(id);
    }

    @Override
    public EventFullDto getEventFullDtoByIdClient(Long id) {
        return publicEventService.getEventFullDtoByIdClient(id);
    }
}