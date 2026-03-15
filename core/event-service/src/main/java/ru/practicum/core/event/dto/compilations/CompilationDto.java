package ru.practicum.core.event.dto.compilations;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.core.event.dto.events.EventShortDto;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompilationDto {

    @Schema(description = "Уникальный идентификатор подборки", example = "1")
    private Long id;

    @Schema(description = "Заголовок подборки", example = "Популярные события")
    private String title;

    @Schema(description = "Флаг закрепления (true — отображается на главной странице)", example = "true")
    private Boolean pinned;

    @Schema(
            description = "Список событий в подборке",
            example = "[...]",
            implementation = EventShortDto.class)
    private List<EventShortDto> events;
}