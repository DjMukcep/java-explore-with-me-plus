package ru.practicum.entity.comment;

import lombok.experimental.UtilityClass;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.NewCommentDto;
import ru.practicum.dto.comment.UserCommentAdminDto;
import ru.practicum.entity.user.User;

import java.time.format.DateTimeFormatter;

@UtilityClass
public class CommentMapper {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static Comment toComment(NewCommentDto comment) {
        return Comment.builder()
                .text(comment.getText())
                .build();
    }

    public static CommentDto toCommentDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .authorId(comment.getAuthor().getId())
                .eventId(comment.getEvent().getId())
                .authorName(comment.getAuthor().getName())
                .text(comment.getText())
                .created(comment.getCreated().format(formatter))
                .updated(comment.getUpdated().format(formatter))
                .build();
    }

    public static UserCommentAdminDto toUserCommentAdminDto(User user) {
        return UserCommentAdminDto.builder()
                .authorId(user.getId())
                .authorName(user.getName())
                .adminWarnCount(user.getAdminWarnings())
                .bannedUntil(user.getBannedUntil().format(formatter))
                .build();
    }
}
