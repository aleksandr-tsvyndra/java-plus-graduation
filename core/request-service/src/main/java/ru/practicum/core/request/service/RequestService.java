package ru.practicum.core.request.service;

import ru.practicum.core.api.exception.ConflictException;
import ru.practicum.core.api.exception.NotFoundException;
import ru.practicum.core.api.internal.request.dto.EventRequestsCountDto;
import ru.practicum.core.request.dto.ChangeRequestStatusDto;
import ru.practicum.core.request.dto.ParticipationRequestDto;
import ru.practicum.core.request.dto.UserParticipationRequestDto;

import java.util.List;

public interface RequestService {

    List<ParticipationRequestDto> getAllRequestsByUser(Long userId);

    ParticipationRequestDto createRequest(Long userId, Long eventId);

    ParticipationRequestDto cancelRequest(Long userId, Long requestId);

    List<ParticipationRequestDto> getUserRequestsForEvent(Long userId, Long eventId);

    UserParticipationRequestDto updateRequestStatus(
            ChangeRequestStatusDto changeRequestStatusDto,
            Long userId,
            Long eventId);

    List<EventRequestsCountDto> getEventRequestsCount(List<Long> eventIds);

    Boolean hasRequest(Long userId, Long eventId);
}