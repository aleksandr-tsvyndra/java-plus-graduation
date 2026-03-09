package ru.practicum.mapper;

import org.mapstruct.*;
import ru.practicum.dto.LocationDto;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.model.Event;
import ru.practicum.model.Location;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface EventMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "categoryId", ignore = true)
    @Mapping(target = "initiatorId", ignore = true)
    @Mapping(target = "confirmedRequests", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "views", ignore = true)
    Event toEvent(NewEventDto newEventDto);

    @Mapping(target = "category.id", source = "categoryId")
    @Mapping(target = "initiator.id", source = "initiatorId")
    EventShortDto toEventShortDto(Event event);

    default Location toLocation(LocationDto dto) {
        if (dto == null) {
            return null;
        }
        Location location = new Location();
        location.setLat(dto.getLat());
        location.setLon(dto.getLon());
        return location;
    }

    default LocationDto toLocationDto(Location location) {
        if (location == null) {
            return null;
        }
        LocationDto locationDto = new LocationDto();
        locationDto.setLat(location.getLat());
        locationDto.setLon(location.getLon());
        return locationDto;
    }

    default EventFullDto toEventFullDtoWithDetails(Event event, CategoryDto category, UserShortDto user) {
        EventFullDto dto = new EventFullDto();
        dto.setId(event.getId());
        dto.setAnnotation(event.getAnnotation());
        dto.setDescription(event.getDescription());
        dto.setEventDate(event.getEventDate());
        dto.setCreatedOn(event.getCreatedOn());
        dto.setPublishedOn(event.getPublishedOn());
        dto.setPaid(event.getPaid());
        dto.setParticipantLimit(event.getParticipantLimit());
        dto.setRequestModeration(event.getRequestModeration());
        dto.setTitle(event.getTitle());

        dto.setState(event.getState() != null ? event.getState().toString() : null);

        dto.setCategory(category);
        dto.setInitiator(user);

        if (event.getLocation() != null) {
            dto.setLocation(toLocationDto(event.getLocation()));
        }

        return dto;
    }

    default EventShortDto toEventShortDtoWithDetails(Event event, CategoryDto category, UserShortDto user) {
        EventShortDto dto = new EventShortDto();

        dto.setId(event.getId());
        dto.setCategory(category);
        dto.setInitiator(user);
        dto.setAnnotation(event.getAnnotation());
        dto.setConfirmedRequests(event.getConfirmedRequests());
        dto.setEventDate(event.getEventDate());
        dto.setPaid(event.getPaid());
        dto.setTitle(event.getTitle());
        dto.setViews(event.getViews());
        return dto;
    }
}