package ru.practicum.core.request.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO для представления результата обработки заявок на участие в событии")
public class UserParticipationRequestDto {

    @Schema(description = "Список подтверждённых заявок", example = "[...]", type = "array")
    private List<ParticipationRequestDto> confirmedRequests;

    @Schema(description = "Список отклонённых заявок", example = "[...]", type = "array")
    private List<ParticipationRequestDto> rejectedRequests;
}
