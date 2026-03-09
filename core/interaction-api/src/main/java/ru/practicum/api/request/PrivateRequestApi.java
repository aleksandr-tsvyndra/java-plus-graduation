package ru.practicum.api.request;

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
import ru.practicum.dto.enums.RequestStatus;
import ru.practicum.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.dto.request.ParticipationRequestDto;

import java.util.List;
import java.util.Map;

@Validated
@RequestMapping("/users")
public interface PrivateRequestApi {

    @GetMapping("/{userId}/events/{eventId}/requests")
    List<ParticipationRequestDto> getEventRequests(@PathVariable @Positive Long userId,
                                                   @PathVariable @Positive Long eventId);

    @PatchMapping("/{userId}/events/{eventId}/requests")
    EventRequestStatusUpdateResult updateRequestStatus(
            @PathVariable @Positive Long userId,
            @PathVariable @Positive Long eventId,
            @RequestBody @Valid EventRequestStatusUpdateRequest updateRequest
    );

    @GetMapping("/{userId}/requests")
    List<ParticipationRequestDto> getUserRequests(@PathVariable @Positive Long userId);

    @PostMapping("/{userId}/requests")
    @ResponseStatus(HttpStatus.CREATED)
    ParticipationRequestDto createRequest(@PathVariable @Positive Long userId,
                                          @RequestParam @Positive Long eventId);

    @PatchMapping("/{userId}/requests/{requestId}/cancel")
    ParticipationRequestDto cancelRequest(@PathVariable @Positive Long userId,
                                          @PathVariable @Positive Long requestId);

    @GetMapping("/client/count")
    Map<Long, List<ParticipationRequestDto>> getConfirmedRequestsCount(
            @RequestParam List<Long> eventIds,
            @RequestParam RequestStatus requestStatus);

    @GetMapping("/{userId}/client/event/{eventId}")
    ParticipationRequestDto getUserRequestByUserIdAndEventId(
            @PathVariable @Positive Long userId,
            @PathVariable @Positive Long eventId);
}
