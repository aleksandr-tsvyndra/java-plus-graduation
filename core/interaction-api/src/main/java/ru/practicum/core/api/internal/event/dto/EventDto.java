package ru.practicum.core.api.internal.event.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;
import lombok.Getter;
import ru.practicum.core.api.util.enums.EventState;
import ru.practicum.core.api.internal.user.dto.UserShortDto;

import java.time.LocalDateTime;


@Setter
@Getter
@Data
@Builder
public class EventDto {
    private Long id;

    @NotNull(message = "Название события не может быть пустым")
    private String title;

    @NotNull(message = "Аннотация события не может быть пустой")
    private String annotation;

    @NotNull(message = "Описание события не может быть пустым")
    private String description;

    private CategoryDto category;

    private UserShortDto initiator;

    @NotNull(message = "Дата события обязательна")
    private LocalDateTime eventDate;

    private LocalDateTime createdOn;

    private LocalDateTime publishedOn;

    private LocationDto location;

    private Integer participantLimit;

    private Boolean paid;

    private Boolean requestModeration;

    private EventState state;

    private Long confirmedRequests;

    private Double rating;
}