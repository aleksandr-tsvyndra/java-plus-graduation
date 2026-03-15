package ru.practicum.core.request.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.core.api.util.enums.RequestStatus;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO для представления заявки на участие в событии")
public class ParticipationRequestDto {

    @Schema(description = "Уникальный идентификатор заявки", example = "1")
    private Long id;

    @NotNull(message = "Идентификатор события не может быть пустым")
    @Schema(description = "Идентификатор события", example = "1001", required = true)
    private Long event;

    @NotNull(message = "Идентификатор пользователя не может быть пустым")
    @Schema(description = "Идентификатор пользователя", example = "2001", required = true)
    private Long requester;

    @Schema(description = "Дата и время создания заявки", example = "2025-04-05T10:30:00")
    private LocalDateTime created;

    @Schema(
            description = "Статус заявки",
            example = "PENDING",
            allowableValues = {"PENDING", "CONFIRMED", "REJECTED", "CANCELED"})
    private RequestStatus status;
}
