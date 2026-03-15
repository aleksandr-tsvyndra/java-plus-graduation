package ru.practicum.core.event.dto.events;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import ru.practicum.core.event.model.enums.events.EventSort;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.core.api.util.constants.PaginationConstants.DEFAULT_FROM;
import static ru.practicum.core.api.util.constants.PaginationConstants.DEFAULT_SIZE;

@Data
@Builder
public class UserEventParams {

    @Schema(description = "Текст для поиска в заголовке и описании события", example = "концерт", nullable = true)
    private String text;

    @Schema(description = "Список идентификаторов категорий", example = "[100, 200]", nullable = true)
    private List<Long> categories;

    @Schema(description = "Признак платности события", example = "true", nullable = true)
    private Boolean paid;

    @Schema(description = "Показывать только события с доступными местами", example = "true", nullable = true)
    private Boolean onlyAvailable;

    @Schema(description = "Начало временного диапазона", example = "2025-04-01T10:00:00", nullable = true)
    private LocalDateTime rangeStart;

    @Schema(description = "Конец временного диапазона", example = "2025-04-07T23:59:59", nullable = true)
    private LocalDateTime rangeEnd;

    @Schema(description = "Критерий сортировки", example = "RATING", allowableValues = {"EVENT_DATE", "RATING"}, required = true)
    @Builder.Default
    private EventSort sort = EventSort.RATING;

    @Schema(description = "Смещение для пагинации", example = "0", minimum = "0", defaultValue = "0")
    @Builder.Default
    private Integer from = Integer.valueOf(DEFAULT_FROM);

    @Schema(description = "Размер страницы", example = "10", minimum = "1", defaultValue = "10")
    @Builder.Default
    private Integer size = Integer.valueOf(DEFAULT_SIZE);
}