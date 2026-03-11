package ru.practicum.service;

import ru.practicum.dto.enums.RequestStatus;
import ru.practicum.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.dto.request.ParticipationRequestDto;

import java.util.List;
import java.util.Map;

public interface RequestService {
    List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateRequestStatus(Long userId, Long eventId,
                                                       EventRequestStatusUpdateRequest updateRequest);

    List<ParticipationRequestDto> getUserRequests(Long userId);

    ParticipationRequestDto createRequest(Long userId, Long eventId);

    ParticipationRequestDto cancelRequest(Long userId, Long requestId);

    Map<Long, List<ParticipationRequestDto>> getConfirmedRequestsCount(List<Long> eventIds, RequestStatus requestStatus);

    ParticipationRequestDto getUserRequestByUserIdAndEventId(Long userId, Long eventId);

}