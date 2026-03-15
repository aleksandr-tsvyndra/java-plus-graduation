package ru.practicum.core.event.dto.comments;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import ru.practicum.core.event.model.enums.comments.CommentStatus;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.core.api.util.constants.DateTimeFormatConstants.DATE_TIME_FORMAT;

@Getter
@Setter
@Builder
public class AdminCommentParams {

    @Schema(description = "Список идентификаторов комментариев", example = "[1, 2, 3]")
    private List<Long> comments;

    @Schema(
            description = "Текст для поиска в комментарии",
            example = "Очень полезное событие для саморазвития!")
    private String text;

    @Schema(description = "Список идентификаторов событий", example = "[100, 101]")
    private List<Long> events;

    @Schema(description = "Список идентификаторов авторов", example = "[10, 11]")
    private List<Long> authors;

    @Schema(
            description = "Список статусов комментариев",
            example = "[PENDING, PUBLISHED]",
            allowableValues = {"PENDING", "PUBLISHED", "REJECTED"})
    private List<CommentStatus> status;

    @DateTimeFormat(pattern = DATE_TIME_FORMAT)
    @Schema(description = "Начальная дата создания комментария", example = "2025-04-01 10:00:00")
    private LocalDateTime createdDateStart;

    @DateTimeFormat(pattern = DATE_TIME_FORMAT)
    @Schema(description = "Конечная дата создания комментария", example = "2025-04-07 23:59:59")
    private LocalDateTime createdDateEnd;

    @DateTimeFormat(pattern = DATE_TIME_FORMAT)
    @Schema(description = "Начальная дата публикации комментария", example = "2025-04-02 12:00:00")
    private LocalDateTime publishedDateStart;

    @DateTimeFormat(pattern = DATE_TIME_FORMAT)
    @Schema(description = "Конечная дата публикации комментария", example = "2025-04-08 23:59:59")
    private LocalDateTime publishedDateEnd;

    @Min(0)
    @Schema(description = "Смещение для пагинации", example = "0", minimum = "0")
    private Integer from;

    @Min(1)
    @Schema(description = "Размер страницы", example = "10", minimum = "1")
    private Integer size;
}