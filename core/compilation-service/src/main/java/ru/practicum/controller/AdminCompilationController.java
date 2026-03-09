package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.api.compilation.AdminCompilationApi;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.compilation.UpdateCompilationRequest;
import ru.practicum.service.CompilationService;

@RestController
@RequiredArgsConstructor
public class AdminCompilationController implements AdminCompilationApi {
    private final CompilationService compilationService;

    @Override
    public CompilationDto createCompilation(NewCompilationDto newCompilationDto) {
        return compilationService.createCompilation(newCompilationDto);
    }

    @Override
    public void deleteCompilation(Long compId) {
        compilationService.deleteCompilation(compId);
    }

    @Override
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateRequest) {
        return compilationService.updateCompilation(compId, updateRequest);
    }
}
