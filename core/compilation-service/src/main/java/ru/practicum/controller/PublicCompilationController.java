package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.api.compilation.PublicCompilationApi;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.service.CompilationService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PublicCompilationController implements PublicCompilationApi {
    private final CompilationService compilationService;

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, int from, int size) {
        return compilationService.getCompilations(pinned, PageRequest.of(from / size, size));
    }

    @Override
    public CompilationDto getCompilationById(Long compId) {
        return compilationService.getCompilationById(compId);
    }
}
