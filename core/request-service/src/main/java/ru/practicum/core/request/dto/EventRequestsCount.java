package ru.practicum.core.request.dto;

public record EventRequestsCount(
        Long eventId,
        Long confirmedRequests
) {}
