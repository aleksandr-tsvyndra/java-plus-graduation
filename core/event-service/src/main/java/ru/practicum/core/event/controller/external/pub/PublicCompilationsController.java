package ru.practicum.core.event.controller.external.pub;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static ru.practicum.core.api.util.constants.PaginationConstants.*;

import ru.practicum.core.api.exception.NotFoundException;
import ru.practicum.core.event.dto.compilations.CompilationDto;
import ru.practicum.core.event.service.api.CompilationService;

@Tag(name = "Public: Подборки событий", description = "Операции для получения информации о подборках событий (публичный доступ)")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/compilations")
public class PublicCompilationsController {
    private final CompilationService compilationService;

    @Operation(summary = "Получить список подборок",
            description = "Возвращает список подборок событий. Возможна фильтрация по флагу закрепления и пагинация.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список подборок успешно получен",
                    content = @Content(schema = @Schema(implementation = List.class, example = "[...]", type = "array"))),
            @ApiResponse(responseCode = "500", description = "Ошибка сервера")
    })
    @GetMapping
    public ResponseEntity<List<CompilationDto>> getAll(
            @RequestParam(required = false) Boolean pinned,
            @RequestParam(defaultValue = DEFAULT_FROM) @Min(value = 0, message = FROM_VALUE_ERROR) int from,
            @RequestParam(defaultValue = DEFAULT_SIZE) @Min(value = 1, message = SIZE_VALUE_ERROR) int size
    ) {
        log.info("GET /compilations?pinned={}&from={}&size={}", pinned, from, size);
        List<CompilationDto> compilations = compilationService.getAll(pinned, from, size);
        log.info("Возвращено {} подборок", compilations.size());
        return ResponseEntity.ok(compilations);
    }

    @Operation(summary = "Получить подборку по ID",
            description = "Возвращает информацию о конкретной подборке событий по её идентификатору.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Информация о подборке успешно получена",
                    content = @Content(schema = @Schema(implementation = CompilationDto.class))),
            @ApiResponse(responseCode = "404", description = "Подборка с указанным ID не найдена"),
            @ApiResponse(responseCode = "500", description = "Ошибка сервера")
    })
    @GetMapping("/{compId}")
    public ResponseEntity<CompilationDto> getById(@PathVariable Long compId) {
        log.info("GET /compilations/{}", compId);
        CompilationDto compilation = compilationService.getById(compId);
        log.info("Подборка с ID={} успешно получена", compId);
        return ResponseEntity.ok(compilation);
    }
}