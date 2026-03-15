package ru.practicum.core.event.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Mappings;
import ru.practicum.core.event.dto.compilations.CompilationDto;
import ru.practicum.core.event.model.Compilation;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {EventMapper.class})
public interface CompilationMapper {

    @Mappings({
            @Mapping(source = "id", target = "id"),
            @Mapping(source = "title", target = "title"),
            @Mapping(source = "pinned", target = "pinned"),
            @Mapping(source = "events", target = "events")
    })
    CompilationDto toDto(Compilation compilation);
}