package ru.practicum.core.event.dto.comments;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import ru.practicum.core.event.model.enums.comments.CommentStatus;

import java.time.LocalDateTime;

@Data
@Builder
public class CommentDto {

    @Schema(description = "Уникальный идентификатор комментария", example = "123")
    private Long id;

    @Schema(description = "Текст комментария", example = "Отличное мероприятие!")
    private String text;

    @Schema(description = "Идентификатор события", example = "1001")
    private Long eventId;

    @Schema(description = "Идентификатор автора комментария", example = "456")
    private Long authorId;

    @Schema(description = "Дата и время создания комментария", example = "2025-04-05T14:30:00")
    private LocalDateTime createdDate;

    @Schema(description = "Дата и время последнего обновления комментария", example = "2025-04-05T15:00:00")
    private LocalDateTime updatedDate;

    @Schema(description = "Дата и время публикации комментария", example = "2025-04-06T08:00:00")
    private LocalDateTime publishedDate;

    @Schema(
            description = "Статус комментария",
            example = "APPROVED",
            allowableValues = {"PENDING", "APPROVED", "REJECTED"})
    private CommentStatus status;
}