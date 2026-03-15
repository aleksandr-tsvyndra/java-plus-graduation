package ru.practicum.core.event.dto.comments;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.practicum.core.event.model.enums.comments.CommentStatus;

@Data
public class CommentPatchDto {

    @NotNull(message = "Статус комментария не может быть null")
    @Schema(
            description = "Новый статус комментария",
            example = "APPROVED",
            allowableValues = {"PENDING", "APPROVED", "REJECTED"},
            required = true)
    private CommentStatus status;
}