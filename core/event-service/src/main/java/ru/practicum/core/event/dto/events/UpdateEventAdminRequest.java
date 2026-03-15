package ru.practicum.core.event.dto.events;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import ru.practicum.core.api.internal.event.dto.LocationDto;
import ru.practicum.core.api.constraint.EventStartDateTime;
import ru.practicum.core.event.model.enums.events.EventStateAction;

import java.time.LocalDateTime;

@Data
@Builder
public class UpdateEventAdminRequest {

    @Nullable
    @Size(min = 20, max = 2000, message = "Аннотация должна содержать от 20 до 2000 символов")
    @Schema(
            description = "Краткое описание события",
            example = "Интересное мероприятие для всех возрастов",
            nullable = true,
            minLength = 20,
            maxLength = 2000)
    private String annotation;

    @Nullable
    @Schema(description = "Идентификатор категории события", example = "1001", nullable = true)
    private Long category;

    @Nullable
    @Size(min = 20, max = 7000, message = "Описание должно содержать от 20 до 7000 символов")
    @Schema(
            description = "Полное описание события",
            example = "На мероприятии будут еда и возможно слабоалкогольные напитки",
            nullable = true,
            minLength = 20,
            maxLength = 7000)
    private String description;

    @Nullable
    @EventStartDateTime
    @Future(message = "Дата события должна быть в будущем")
    @Schema(description = "Дата и время начала события", example = "2025-04-10T14:00:00", nullable = true)
    private LocalDateTime eventDate;

    @Nullable
    @Schema(description = "Геолокация события", implementation = LocationDto.class, nullable = true)
    private LocationDto location;

    @Nullable
    @Schema(description = "Признак платности события", example = "false", nullable = true)
    private Boolean paid;

    @Nullable
    @Min(value = 0, message = "Лимит участников не может быть отрицательным")
    @Schema(description = "Максимальное количество участников", example = "100", nullable = true, minimum = "0")
    private Integer participantLimit;

    @Nullable
    @Schema(description = "Признак необходимости модерации заявок", example = "true", nullable = true)
    private Boolean requestModeration;

    @Nullable
    @Schema(
            description = "Действие над событием",
            example = "PUBLISH_EVENT",
            allowableValues = {"PUBLISH_EVENT", "REJECT_EVENT", "SEND_TO_REVIEW", "CANCEL_REVIEW"},
            nullable = true)
    private EventStateAction stateAction;

    @Nullable
    @Size(min = 3, max = 120, message = "Название события должно содержать от 3 до 120 символов")
    @Schema(
            description = "Название события",
            example = "Конференция по компьютерным технологиям",
            nullable = true,
            minLength = 3,
            maxLength = 120)
    private String title;
}