package ru.practicum.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import ru.practicum.EndpointHitDto;
import ru.practicum.StatResponseDto;
import ru.practicum.model.entity.Stat;

import java.time.LocalDateTime;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface StatMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "created", qualifiedByName = "getTimestampToCreated", source = "timestamp")
    Stat toStat(EndpointHitDto endpointHitDto);

    StatResponseDto toStatResponseDto(Stat stat);

    @Named("getTimestampToCreated")
    default LocalDateTime getTimestampToCreated(LocalDateTime timestamp) {
        return timestamp;
    }
}