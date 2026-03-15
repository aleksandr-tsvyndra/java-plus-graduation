package ru.practicum.core.api.internal.event.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LocationDto {

    @NotNull(message = "Широта обязательна и должна быть числом")
    private Float lat;

    @NotNull(message = "Долгота обязательна и должна быть числом")
    private Float lon;
}