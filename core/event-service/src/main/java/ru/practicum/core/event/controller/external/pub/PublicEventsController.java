package ru.practicum.core.event.controller.external.pub;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.core.api.util.constants.DateTimeFormatConstants.DATE_TIME_FORMAT;
import static ru.practicum.core.api.util.constants.PaginationConstants.*;

import ru.practicum.core.api.internal.event.dto.EventDto;
import ru.practicum.core.event.dto.events.UserEventParams;
import ru.practicum.core.event.model.enums.events.EventSort;
import ru.practicum.core.event.service.api.EventService;

@Tag(name = "Public: События", description = "Операции для получения информации о мероприятиях (публичный доступ)")
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/events")
public class PublicEventsController {
    private static final String USER_ID_HEADER = "X-EWM-USER-ID";

    private final EventService eventService;

    @Operation(summary = "Получить список мероприятий",
            description = "Возвращает список мероприятий, соответствующих заданным фильтрам. Доступно всем пользователям.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список мероприятий успешно получен",
                    content = @Content(schema = @Schema(implementation = List.class, example = "[...]", type = "array"))),
            @ApiResponse(responseCode = "500", description = "Ошибка сервера")
    })
    @GetMapping
    public ResponseEntity<List<EventDto>> getEvents(
            HttpServletRequest request,
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false) Boolean onlyAvailable,
            @RequestParam(required = false) @DateTimeFormat(pattern = DATE_TIME_FORMAT) LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = DATE_TIME_FORMAT) LocalDateTime rangeEnd,
            @RequestParam(required = false) EventSort sort,
            @RequestParam(defaultValue = DEFAULT_FROM) @Min(value = 0, message = FROM_VALUE_ERROR) int from,
            @RequestParam(defaultValue = DEFAULT_SIZE) @Min(value = 1, message = SIZE_VALUE_ERROR) int size
    ) {
        log.info("GET /events?text={}&categories={}&paid={}&onlyAvailable={}&rangeStart={}&rangeEnd={}&sort={}&from={}&size={}",
                text, categories, paid, onlyAvailable, rangeStart, rangeEnd, sort, from, size);

        UserEventParams userEventParams = UserEventParams.builder()
                .text(text)
                .categories(categories)
                .paid(paid)
                .onlyAvailable(onlyAvailable)
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .sort(sort)
                .from(from)
                .size(size)
                .build();

        List<EventDto> events = eventService.findAllByUserParams(userEventParams);
        log.info("Возвращено {} мероприятий", events.size());

        return ResponseEntity.ok(events);
    }

    @Operation(summary = "Получить мероприятие по ID",
            description = "Возвращает информацию о конкретном мероприятии по его идентификатору.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Информация о мероприятии успешно получена",
                    content = @Content(schema = @Schema(implementation = EventDto.class))),
            @ApiResponse(responseCode = "404", description = "Мероприятие не найдено"),
            @ApiResponse(responseCode = "500", description = "Ошибка сервера")
    })
    @GetMapping("/{eventId}")
    public ResponseEntity<EventDto> getPublishedEvent(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @NotNull @PathVariable Long eventId
    ) {
        log.info("GET /events/{} with request header: {}={}", eventId, USER_ID_HEADER, userId);

        EventDto eventDto = eventService.findPublishedEvent(eventId, userId);
        log.info("Возвращено мероприятие с ID={} по запросу пользователя с ID={}", eventDto.getId(), userId);

        return ResponseEntity.ok(eventDto);
    }

    @Operation(summary = "Получить рекомендации",
            description = "Возвращает список рекомендуемых мероприятий для пользователя.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список рекомендаций успешно получен",
                    content = @Content(schema = @Schema(implementation = List.class, example = "[...]", type = "array"))),
            @ApiResponse(responseCode = "500", description = "Ошибка сервера")
    })
    @GetMapping("/recommendations")
    public ResponseEntity<List<EventDto>> getRecommendations(@RequestHeader(USER_ID_HEADER) Long userId) {
        log.info("GET /events/recommendations with request header: {}={}", USER_ID_HEADER, userId);

        List<EventDto> recommendations = eventService.getRecommendations(userId);
        log.info("Возвращено {} рекомендаций для пользователя с ID={}", recommendations.size(), userId);

        return ResponseEntity.ok(recommendations);
    }

    @Operation(summary = "Добавить лайк к мероприятию",
            description = "Позволяет пользователю поставить лайк на мероприятие.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Лайк успешно добавлен"),
            @ApiResponse(responseCode = "404", description = "Мероприятие не найдено"),
            @ApiResponse(responseCode = "500", description = "Ошибка сервера")
    })
    @PutMapping("/{eventId}/like")
    public ResponseEntity<Void> addLike(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @PathVariable Long eventId
    ) {
        log.info("PUT /events/{}/like with request header: {}={}", eventId, USER_ID_HEADER, userId);

        eventService.addLike(eventId, userId);
        return ResponseEntity.noContent().build();
    }
}