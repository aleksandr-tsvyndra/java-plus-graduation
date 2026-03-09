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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.dto.event.UpdateEventUserRequest;

import java.util.List;

@Validated
public interface PrivateEventApi {

    @GetMapping("/users/{userId}/events")
    List<EventShortDto> getEventsByUser(@PathVariable @Positive Long userId,
                                        @RequestParam(defaultValue = "0") int from,
                                        @RequestParam(defaultValue = "10") int size);

    @PostMapping("/users/{userId}/events")
    @ResponseStatus(HttpStatus.CREATED)
    EventFullDto createEvent(@PathVariable @Positive Long userId,
                             @RequestBody @Valid NewEventDto newEventDto);

    @GetMapping("/users/{userId}/events/{eventId}")
    EventFullDto getEventByUser(@PathVariable @Positive Long userId,
                                @PathVariable @Positive Long eventId);

    @PatchMapping("/users/{userId}/events/{eventId}")
    EventFullDto updateEventByUser(@PathVariable @Positive Long userId,
                                   @PathVariable @Positive Long eventId,
                                   @RequestBody @Valid UpdateEventUserRequest updateRequest);

}
