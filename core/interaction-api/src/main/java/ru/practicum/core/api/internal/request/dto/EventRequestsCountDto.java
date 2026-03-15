package ru.practicum.core.api.internal.request.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import jakarta.validation.constraints.NotNull;

@Data
@AllArgsConstructor
public class EventRequestsCountDto {

    @NotNull(message = "ID события не может быть null")
    private Long eventId;

    private Long confirmedRequests;
}