package ru.practicum.entity.comment;

import ru.practicum.dto.comment.UserCommentAdminDto;

import java.time.LocalDateTime;

public interface CommentService {

    UserCommentAdminDto giveWarning(Long commentId);

    Comment getById(Long id);

    UserCommentAdminDto updateUserBan(Long userId, LocalDateTime banDate);

    void adminDelete(Long commentId);
}
