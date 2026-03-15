package ru.practicum.core.event.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.practicum.core.event.dto.comments.CommentDto;
import ru.practicum.core.event.model.Comment;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CommentMapper {

    @Mapping(target = "eventId", source = "comment.event.id")
    CommentDto toDto(Comment comment);

    @Mapping(target = "eventId", source = "comment.event.id")
    List<CommentDto> toDto(List<Comment> comments);
}