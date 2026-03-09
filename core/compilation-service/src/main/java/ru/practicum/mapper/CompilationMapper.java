package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.model.Compilation;

import java.util.Set;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CompilationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eventsId", source = "events")
    Compilation toCompilation(NewCompilationDto dto);

    @Mapping(target = "events", ignore = true)
    CompilationDto toCompilationDtoTemp(Compilation compilation);

    default CompilationDto toCompilationDto(Compilation compilation, Set<EventShortDto> events) {
        CompilationDto dto = toCompilationDtoTemp(compilation);
        dto.setEvents(events);
        return dto;
    }
}
