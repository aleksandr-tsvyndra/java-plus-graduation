package ru.practicum.api.event;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.dto.event.UpdateEventUserRequest;

import java.util.List;

@Validated
@RequestMapping("/users/{userId}/events")
public interface PrivateEventApi {

    @GetMapping
    List<EventShortDto> getEventsByUser(@PathVariable @Positive Long userId,
                                        @RequestParam(defaultValue = "0") int from,
                                        @RequestParam(defaultValue = "10") int size);

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    EventFullDto createEvent(@PathVariable @Positive Long userId,
                             @RequestBody @Valid NewEventDto newEventDto);

    @GetMapping("/{eventId}")
    EventFullDto getEventByUser(@PathVariable @Positive Long userId,
                                @PathVariable @Positive Long eventId);

    @PatchMapping("/{eventId}")
    EventFullDto updateEventByUser(@PathVariable @Positive Long userId,
                                   @PathVariable @Positive Long eventId,
                                   @RequestBody @Valid UpdateEventUserRequest updateRequest);

}
