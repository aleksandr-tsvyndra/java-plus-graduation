package ru.practicum.core.event.dto.compilations;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateCompilationRequest {

    @Schema(description = "Список идентификаторов событий", example = "[100, 200, 300]", nullable = true)
    private Set<Long> events;

    @Schema(
            description = "Флаг закрепления подборки",
            example = "false",
            nullable = true)
    private Boolean pinned;

    @Size(min = 1, max = 50, message = "Заголовок должен быть от 1 до 50 символов")
    @Schema(
            description = "Заголовок подборки",
            example = "События этой недели",
            required = true,
            minLength = 1,
            maxLength = 50)
    private String title;
}