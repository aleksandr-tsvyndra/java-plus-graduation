package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.api.request.PrivateRequestApi;
import ru.practicum.dto.enums.RequestStatus;
import ru.practicum.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.service.RequestService;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class PrivateRequestController implements PrivateRequestApi {
    private final RequestService participationRequestService;

    @Override
    public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        return participationRequestService.getEventRequests(userId, eventId);
    }

    @Override
    public EventRequestStatusUpdateResult updateRequestStatus(Long userId,
                                                              Long eventId,
                                                              EventRequestStatusUpdateRequest updateRequest) {
        return participationRequestService.updateRequestStatus(userId, eventId, updateRequest);
    }

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        return participationRequestService.getUserRequests(userId);
    }

    @Override
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        return participationRequestService.createRequest(userId, eventId);
    }

    @Override
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        return participationRequestService.cancelRequest(userId, requestId);
    }

    @Override
    public Map<Long, List<ParticipationRequestDto>> getConfirmedRequestsCount(List<Long> eventIds,
                                                                              RequestStatus requestStatus) {
        return participationRequestService.getConfirmedRequestsCount(eventIds, requestStatus);
    }

    @Override
    public ParticipationRequestDto getUserRequestByUserIdAndEventId(Long userId, Long eventId) {
        return participationRequestService.getUserRequestByUserIdAndEventId(userId, eventId);
    }
}