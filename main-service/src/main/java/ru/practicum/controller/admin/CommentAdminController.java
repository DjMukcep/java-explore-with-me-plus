package ru.practicum.controller.admin;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.comment.UserCommentAdminDto;
import ru.practicum.entity.comment.CommentService;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/admin/comments")
@RequiredArgsConstructor
public class CommentAdminController {

    private final CommentService commentService;

    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    @PatchMapping("/{commentId}")
    public UserCommentAdminDto warnUser(@PathVariable @Positive Long commentId) {
        return commentService.giveWarning(commentId);
    }

    @PatchMapping("/users/{userId}")
    public UserCommentAdminDto updateUserBan(@PathVariable @Positive Long userId,
                                             @RequestParam(required = false) @DateTimeFormat(pattern = DATE_TIME_PATTERN) @Future LocalDateTime banDate) {

        return commentService.updateUserBan(userId, banDate);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable @Positive Long commentId) {
        commentService.adminDelete(commentId);
    }
}
