package ru.practicum.core.event.service.api;

import ru.practicum.core.event.dto.comments.AdminCommentParams;
import ru.practicum.core.event.dto.comments.CommentDto;
import ru.practicum.core.event.dto.comments.NewCommentDto;
import ru.practicum.core.event.model.enums.comments.CommentStatus;

import java.util.List;

public interface CommentService {

    List<CommentDto> findComments(long eventId);

    CommentDto findComment(long eventId, long commentId);

    CommentDto findCommentById(long commentId);

    CommentDto createComment(long userId, long eventId, NewCommentDto newCommentDto);

    void deleteComment(long commentId);

    CommentDto patchCommentStatus(long commentId, CommentStatus status);

    List<CommentDto> findApprovedCommentsOnUserId(long userId);

    List<CommentDto> findAllByAdminParams(AdminCommentParams params);

}