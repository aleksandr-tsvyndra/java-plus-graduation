package ru.practicum.api.event;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Validated
public interface PublicEventApi {

    @GetMapping("/events")
    List<EventShortDto> getEvents(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "false") Boolean onlyAvailable,
            @RequestParam(required = false) String sort,
            @PositiveOrZero @RequestParam(defaultValue = "0") int from,
            @Positive @RequestParam(defaultValue = "10") int size,
            HttpServletRequest httpRequest);

    @GetMapping("/events/{id}")
    EventFullDto getEventById(@PathVariable @Positive Long id,
                              HttpServletRequest httpRequest);

    @GetMapping("/events/client/validate/category/{categoryId}")
    void validateCategoryHasNoEvents(@PathVariable @Positive Long categoryId);

    @GetMapping("/events/client/find/all")
    Set<EventShortDto> getEventShortDtoSetByIds(@RequestParam Set<Long> eventIds);

    @GetMapping("/events/client/short/{id}")
    EventShortDto getEventShortDtoByIdClient(@PathVariable @Positive Long id);

    @GetMapping("/events/client/full/{id}")
    EventFullDto getEventFullDtoByIdClient(@PathVariable @Positive Long id);

    @GetMapping("/events/client/validate/{eventId}")
    void validateEventExistingById(@PathVariable @Positive Long eventId);
}
