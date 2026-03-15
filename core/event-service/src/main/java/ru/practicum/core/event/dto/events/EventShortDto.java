package ru.practicum.core.event.dto.events;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.core.api.internal.event.dto.CategoryDto;
import ru.practicum.core.api.internal.user.dto.UserShortDto;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventShortDto {

    @Schema(description = "Краткое описание события", example = "Интересное мероприятие для всех возрастов")
    private String annotation;

    @Schema(description = "Категория события", implementation = CategoryDto.class)
    private CategoryDto category;

    @Schema(description = "Количество подтверждённых заявок", example = "150")
    private Integer confirmedRequests;

    @Schema(description = "Дата и время начала события", example = "2025-04-10T14:00:00")
    private LocalDateTime eventDate;

    @Schema(description = "Уникальный идентификатор события", example = "1001")
    private Long id;

    @Schema(description = "Инициатор события", implementation = UserShortDto.class)
    private UserShortDto initiator;

    @Schema(description = "Признак платности события", example = "true")
    private Boolean paid;

    @Schema(description = "Название события", example = "Международный фестиваль искусств")
    private String title;

    @Schema(description = "Рейтинг события", example = "4.7")
    private Double rating;
}