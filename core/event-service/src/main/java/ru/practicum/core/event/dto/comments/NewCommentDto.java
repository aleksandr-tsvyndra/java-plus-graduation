package ru.practicum.core.event.dto.comments;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NewCommentDto {

    @NotBlank(message = "Текст комментария не может быть пустым")
    @Size(min = 20, max = 7000, message = "Текст комментария должен содержать от 20 до 7000 символов")
    @Schema(
            description = "Текст комментария",
            example = "Это очень интересное и полезное мероприятие!",
            required = true,
            minLength = 20,
            maxLength = 7000)
    private String text;
}