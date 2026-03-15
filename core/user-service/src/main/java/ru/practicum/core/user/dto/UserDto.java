package ru.practicum.core.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO для представления данных пользователя")
public class UserDto {

    @Schema(description = "Уникальный идентификатор пользователя", example = "1")
    private Long id;

    @NotBlank(message = "Имя не может быть пустым")
    @Size(max = 250, message = "Максимальная длина имени — 250 символов")
    @Schema(description = "Имя пользователя", example = "Иван Иванов", required = true)
    private String name;

    @NotBlank(message = "Email не может быть пустым")
    @Size(max = 254, message = "Максимальная длина email — 254 символа")
    @Email(message = "Некорректный формат email")
    @Schema(description = "Электронная почта пользователя", example = "ivan.ivanov@example.com", required = true)
    private String email;
}
