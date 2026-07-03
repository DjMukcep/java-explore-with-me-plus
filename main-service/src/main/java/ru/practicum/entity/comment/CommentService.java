package ru.practicum.entity.comment;

import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.NewCommentDto;
import ru.practicum.dto.comment.UpdateCommentDto;
import ru.practicum.dto.comment.UserCommentAdminDto;

import java.util.List;

public interface CommentService {

    CommentDto createComment(NewCommentDto newCommentDto, Long userId);

    CommentDto updateComment(UpdateCommentDto updateCommentDto);

    void deleteComment(Long commentId, Long userId);

    List<CommentDto> getComments(Long userId);

    UserCommentAdminDto giveWarning(Long commentId);

    Comment getById(Long id);

    void adminDelete(Long commentId);
}
