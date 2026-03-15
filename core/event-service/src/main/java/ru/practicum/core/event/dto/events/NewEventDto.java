package ru.practicum.core.event.dto.events;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import ru.practicum.core.api.internal.event.dto.LocationDto;
import ru.practicum.core.api.config.CustomLocalDateTimeDeserializer;
import ru.practicum.core.api.config.CustomLocalDateTimeSerializer;
import ru.practicum.core.api.constraint.EventStartDateTime;
import ru.practicum.core.event.model.enums.events.EventStateAction;

import java.time.LocalDateTime;

@Data
@Builder
public class NewEventDto {

    @NotBlank(message = "Заголовок события не может быть пустым")
    @Size(min = 3, max = 120, message = "Заголовок события должен содержать от 3 до 120 символов")
    @Schema(
            description = "Заголовок события",
            example = "Фестиваль науки",
            required = true,
            minLength = 3,
            maxLength = 120)
    private String title;

    @NotBlank(message = "Аннотация события не может быть пустой")
    @Size(min = 20, max = 2000, message = "Аннотация события должна содержать от 20 до 2000 символов")
    @Schema(
            description = "Краткое описание события",
            example = "Интересное мероприятие для всех возрастов",
            required = true,
            minLength = 20,
            maxLength = 2000)
    private String annotation;

    @NotBlank(message = "Описание события не может быть пустым")
    @Size(min = 20, max = 7000, message = "Описание события должно содержать от 20 до 7000 символов")
    @Schema(
            description = "Полное описание события",
            example = "На фестивале будут представлены научные выставки и лекции",
            required = true,
            minLength = 20,
            maxLength = 7000)
    private String description;

    @NotNull(message = "Категория события обязательна")
    @Schema(description = "Идентификатор категории события", example = "1001", required = true)
    private Long category;

    @NotNull(message = "Дата события обязательна")
    @JsonDeserialize(using = CustomLocalDateTimeDeserializer.class) // Для входящих данных
    @JsonSerialize(using = CustomLocalDateTimeSerializer.class)     // Для исходящих данных
    @EventStartDateTime
    @Future(message = "Дата события должна быть в будущем")
    @Schema(description = "Дата и время начала события", example = "2025-04-10T14:00:00", required = true)
    private LocalDateTime eventDate;

    @NotNull(message = "Геолокация события обязательна")
    @Schema(description = "Геолокация события", implementation = LocationDto.class, required = true)
    private LocationDto location;

    @Min(value = 0, message = "Лимит участников не может быть отрицательным")
    @Schema(description = "Максимальное количество участников", example = "100", minimum = "0")
    private Integer participantLimit;

    @Schema(description = "Признак платности события", example = "false")
    private Boolean paid;

    @Schema(description = "Признак необходимости модерации заявок", example = "true")
    private Boolean requestModeration;

    @Schema(
            description = "Действие над событием",
            allowableValues = {"SEND_TO_REVIEW", "CANCEL_REVIEW", "PUBLISH_EVENT", "REJECT_EVENT"})
    private EventStateAction stateAction;
}