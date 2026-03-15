package ru.practicum.core.event.dto.compilations;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
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
public class NewCompilationDto {

    @Schema(description = "Список идентификаторов событий для подборки", example = "[100, 200, 300]")
    private Set<Long> events;

    @Schema(description = "Флаг закрепления (true — отображается на главной странице)", example = "false")
    @Builder.Default
    private Boolean pinned = false;

    @NotBlank(message = "Заголовок не может быть пустым")
    @Size(min = 1, max = 50, message = "Заголовок должен быть от 1 до 50 символов")
    @Schema(
            description = "Заголовок подборки",
            example = "События этой недели",
            required = true,
            minLength = 1,
            maxLength = 50)
    private String title;
}