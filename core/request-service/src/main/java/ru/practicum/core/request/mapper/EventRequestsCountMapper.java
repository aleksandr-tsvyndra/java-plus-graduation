package ru.practicum.core.request.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import ru.practicum.core.api.internal.request.dto.EventRequestsCountDto;
import ru.practicum.core.request.dto.EventRequestsCount;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface EventRequestsCountMapper {

    EventRequestsCountDto toDto(EventRequestsCount requestCount);

}
