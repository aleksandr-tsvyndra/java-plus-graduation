package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.compilation.UpdateCompilationRequest;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.exception.NotFoundException;
import ru.practicum.feignClient.PublicEventClient;
import ru.practicum.mapper.CompilationMapper;
import ru.practicum.model.Compilation;
import ru.practicum.repository.CompilationRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final CompilationMapper compilationMapper;

    private final PublicEventClient eventServiceClient;

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, Pageable pageable) {
        if (pinned != null) {
            return compilationRepository.findByPinned(pinned, pageable)
                    .map(compilation -> compilationMapper.toCompilationDto(
                            compilation,
                            eventServiceClient.getEventShortDtoSetByIds(compilation.getEventsId())
                    ))
                    .getContent();
        } else {
            return compilationRepository.findAll(pageable)
                    .map(compilation -> compilationMapper.toCompilationDto(
                            compilation,
                            eventServiceClient.getEventShortDtoSetByIds(compilation.getEventsId())
                    ))
                    .getContent();
        }
    }

    @Override
    public CompilationDto getCompilationById(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation not found"));

        Set<EventShortDto> events = eventServiceClient.getEventShortDtoSetByIds(compilation.getEventsId());

        return compilationMapper.toCompilationDto(compilation, events);
    }

    @Override
    @Transactional
    public CompilationDto createCompilation(NewCompilationDto newCompilationDto) {
        Compilation compilation = compilationMapper.toCompilation(newCompilationDto);
        Set<EventShortDto> events = new HashSet<>();

        if (newCompilationDto.getEvents() != null && !newCompilationDto.getEvents().isEmpty()) {
            events = eventServiceClient.getEventShortDtoSetByIds(newCompilationDto.getEvents());
            if (events.size() != newCompilationDto.getEvents().size()) {
                throw new NotFoundException("Некоторые события не найдены");
            }
            compilation.setEventsId(newCompilationDto.getEvents());
        }

        Compilation savedCompilation = compilationRepository.save(compilation);

        return compilationMapper.toCompilationDto(savedCompilation, events);
    }

    @Override
    @Transactional
    public void deleteCompilation(Long compId) {
        if (!compilationRepository.existsById(compId)) {
            throw new NotFoundException("Compilation not found");
        }
        compilationRepository.deleteById(compId);
    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateRequest) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation not found"));
        Set<EventShortDto> events = new HashSet<>();


        if (updateRequest.getEvents() != null) {
            if (!updateRequest.getEvents().isEmpty()) {
                events = eventServiceClient.getEventShortDtoSetByIds(updateRequest.getEvents());
                if (events.size() != updateRequest.getEvents().size()) {
                    throw new NotFoundException("Некоторые события не найдены");
                }
            }
            compilation.setEventsId(updateRequest.getEvents());
        }

        if (updateRequest.getPinned() != null) {
            compilation.setPinned(updateRequest.getPinned());
        }
        if (updateRequest.getTitle() != null) {
            compilation.setTitle(updateRequest.getTitle());
        }
        Compilation updatedCompilation = compilationRepository.save(compilation);

        return compilationMapper.toCompilationDto(updatedCompilation, events);
    }
}
