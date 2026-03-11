package ru.practicum.api.compilation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.compilation.UpdateCompilationRequest;

@Validated
public interface AdminCompilationApi {

    @PostMapping("/admin/compilations")
    @ResponseStatus(HttpStatus.CREATED)
    CompilationDto createCompilation(@RequestBody @Valid NewCompilationDto newCompilationDto);

    @DeleteMapping("/admin/compilations/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteCompilation(@PathVariable @Positive Long compId);

    @PatchMapping("/admin/compilations/{compId}")
    CompilationDto updateCompilation(@PathVariable @Positive Long compId,
                                     @RequestBody @Valid UpdateCompilationRequest updateRequest);

}
