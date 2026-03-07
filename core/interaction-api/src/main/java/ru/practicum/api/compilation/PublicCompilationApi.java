package ru.practicum.api.compilation;

import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.dto.compilation.CompilationDto;

import java.util.List;

@Validated
@RequestMapping("/compilations")
public interface PublicCompilationApi {

    @GetMapping
    List<CompilationDto> getCompilations(@RequestParam(required = false) Boolean pinned,
                                         @RequestParam(defaultValue = "0") int from,
                                         @RequestParam(defaultValue = "10") int size);

    @GetMapping("/{compId}")
    CompilationDto getCompilationById(@PathVariable @Positive Long compId);

}
