package ru.practicum.entity.comment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.UserCommentAdminDto;
import ru.practicum.entity.event.EventService;
import ru.practicum.entity.user.User;
import ru.practicum.entity.user.UserService;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;

import java.time.LocalDateTime;

@Service
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserService userService;
    private final EventService eventService;


    @Override
    public Comment getById(Long id) {
        return commentRepository.findWithAuthorById(id).orElseThrow(
                () -> new NotFoundException("Comment not found with id: " + id)
        );
    }

    @Override
    @Transactional
    public UserCommentAdminDto giveWarning(Long commentId) {
        Comment comment = getById(commentId);

        User author = comment.getAuthor();

        if (comment.isAdminWarning()) {
            throw new ConflictException(
                    String.format("User with id %s already received warning for comment with id %s", author.getId(), commentId)
            );
        }
        comment.setAdminWarning(true);

        if (author.getAdminWarnings() < 2) {
            log.info("Пользователь получил предупреждение: {}", author);
            author.setAdminWarnings(author.getAdminWarnings() + 1);
            return CommentMapper.toUserCommentAdminDto(author);
        }

        if (author.getBannedUntil() != null && author.getBannedUntil().isAfter(LocalDateTime.now())) {
            throw new ConflictException("User already banned");
        }

        log.info("Пользователь заблокирован из-за превышения лимита предупреждений: {}", author);
        author.setAdminWarnings(0);
        author.setBannedUntil(LocalDateTime.now().plusMonths(6));
        return CommentMapper.toUserCommentAdminDto(author);
    }

    @Override
    @Transactional
    public UserCommentAdminDto updateUserBan(Long userId, LocalDateTime banDate) {
        User user = userService.findById(userId);

        if (banDate != null) {
            if (user.getBannedUntil() != null && user.getBannedUntil().isAfter(LocalDateTime.now())) {
                throw new ConflictException("User already banned");
            }

            log.info("Пользователь получил блокировку до {}: {}", banDate,  user);
            user.setBannedUntil(banDate);
        } else {
            if (user.getBannedUntil() == null || user.getBannedUntil().isBefore(LocalDateTime.now())) {
                throw new ConflictException("User not banned");
            }

            log.info("Пользователь разблокирован:{}", user);
            user.setBannedUntil(null);
        }

        return CommentMapper.toUserCommentAdminDto(user);
    }

    @Override
    @Transactional
    public void adminDelete(Long commentId) {
        Comment comment = getById(commentId);
        CommentDto dto = CommentMapper.toCommentDto(comment);
        log.info("Комментарий удален администратором: {}", CommentMapper.toLogComment(dto));
        commentRepository.delete(comment);
    }
}
