package ru.practicum.core.event.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Mappings;
import ru.practicum.core.api.internal.user.dto.UserShortDto;
import ru.practicum.core.api.internal.event.dto.EventDto;
import ru.practicum.core.event.dto.events.NewEventDto;
import ru.practicum.core.event.dto.events.UpdateEventAdminRequest;
import ru.practicum.core.event.dto.events.UpdateEventUserRequest;
import ru.practicum.core.event.model.Event;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {CategoryMapper.class}
)
public interface EventMapper {

    @Mapping(target = "state", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "locationLon", source = "location.lon")
    @Mapping(target = "locationLat", source = "location.lat")
    @Mapping(target = "initiatorId", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "category", ignore = true)
    Event toModel(NewEventDto newEventDto);

    @Mappings({
            @Mapping(target = "rating", ignore = true),
            @Mapping(target = "confirmedRequests", ignore = true),
            @Mapping(source = "locationLat", target = "location.lat"),
            @Mapping(source = "locationLon", target = "location.lon"),
            @Mapping(target = "initiator", ignore = true)
    })
    EventDto toDto(Event event);

    @Mappings({
            @Mapping(target = "id", source = "event.id"),
            @Mapping(target = "rating", ignore = true),
            @Mapping(target = "confirmedRequests", ignore = true),
            @Mapping(source = "event.locationLat", target = "location.lat"),
            @Mapping(source = "event.locationLon", target = "location.lon"),
            @Mapping(target = "initiator", source = "userShortDto")
    })
    EventDto toDto(Event event, UserShortDto userShortDto);

    NewEventDto toNewEventDto(UpdateEventAdminRequest updateEventAdminRequest);

    NewEventDto toNewEventDto(UpdateEventUserRequest updateEventUserRequest);
}