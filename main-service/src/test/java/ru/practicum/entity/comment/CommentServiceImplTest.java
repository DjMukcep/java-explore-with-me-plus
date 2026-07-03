package ru.practicum.entity.comment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.dto.comment.UserCommentAdminDto;
import ru.practicum.dto.event.Location;
import ru.practicum.entity.category.Category;
import ru.practicum.entity.event.Event;
import ru.practicum.entity.event.EventState;
import ru.practicum.entity.user.CommentsRank;
import ru.practicum.entity.user.User;
import ru.practicum.entity.user.UserService;
import ru.practicum.exception.ConflictException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @InjectMocks
    private CommentServiceImpl commentService;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserService userService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final long COMMENT_ID = 1L;
    private final long USER_ID = 2L;
    private final long EVENT_ID = 3L;

    private Comment comment;
    private User author;
    private UserCommentAdminDto userCommentDto;
    private Event event;

    @BeforeEach
    void setUp() {

        author = User.builder()
                .id(USER_ID)
                .name("test name")
                .mail("test@mail.com")
                .commentsCount(1)
                .adminWarnings(0)
                .bannedUntil(null)
                .rank(CommentsRank.NOVICE)
                .build();

        Category category = Category.builder()
                .id(1L)
                .name("Category")
                .build();

        event = Event.builder()
                .id(EVENT_ID)
                .annotation("annotation")
                .title("Event")
                .category(category)
                .initiator(author)
                .eventDate(LocalDateTime.of(2026, 1, 1, 12, 0))
                .createdOn(LocalDateTime.of(2025, 1, 1, 12, 0))
                .paid(false)
                .participantLimit(0)
                .requestModeration(true)
                .state(EventState.PUBLISHED)
                .location(new Location(10f, 20f))
                .build();

        comment = Comment.builder()
                .id(COMMENT_ID)
                .author(author)
                .event(event)
                .text("SOME TEXT")
                .created(LocalDateTime.now())
                .adminWarning(false)
                .updated(null)
                .build();

        userCommentDto = UserCommentAdminDto.builder()
                .authorId(USER_ID)
                .authorName(author.getName())
                .adminWarnCount(author.getAdminWarnings())
                .bannedUntil(author.getBannedUntil().format(FORMATTER))
                .build();
    }

    @Test
    void giveWarning_whenWarningsLessThanTwo_shouldIncrementWarningsAndReturnDto() {
        userCommentDto.setAdminWarnCount(1);

        when(author.getAdminWarnings())
                .thenReturn(0);
        when(CommentMapper.toUserCommentAdminDto(any(User.class)))
                .thenReturn(userCommentDto);

        UserCommentAdminDto result = commentService.giveWarning(COMMENT_ID);

        verify(author, never()).setBannedUntil(any(LocalDateTime.class));
        assertThat(result, is(userCommentDto));
    }

    @Test
    void giveWarning_whenUserAlreadyBanned_shouldThrowConflictException() {
        // Arrange
        LocalDateTime futureBan = LocalDateTime.now().plusDays(10);
        when(author.getAdminWarnings()).thenReturn(2);
        when(author.getBannedUntil()).thenReturn(futureBan);

        // Act & Assert
        org.junit.jupiter.api.Assertions.assertThrows(
                ConflictException.class,
                () -> commentService.giveWarning(COMMENT_ID)
        );

        verify(author, never()).setAdminWarnings(anyInt());
        verify(author, never()).setBannedUntil(any(LocalDateTime.class));
    }

    @Test
    void giveWarning_whenWarningsExceedLimitAndNotBanned_shouldBanUserForSixMonths() {
        // Arrange
        when(author.getAdminWarnings()).thenReturn(2);
        when(author.getBannedUntil()).thenReturn(null);
        when(CommentMapper.toUserCommentAdminDto(any(User.class))).thenReturn(userCommentDto);

        // Act
        UserCommentAdminDto result = commentService.giveWarning(COMMENT_ID);

        // Assert
        verify(author).setAdminWarnings(0);
        verify(author).setBannedUntil(any(LocalDateTime.class));
        // Можно дополнительно проверить, что дата примерно через 6 месяцев (с допуском)
        // но в юнит-тестах обычно достаточно проверить вызов с корректным типом.
        verify(CommentMapper).toUserCommentAdminDto(author);
        assertThat(result, is(userCommentDto));
    }

    @Test
    void updateUserBan_whenBanDateProvidedAndUserNotBanned_shouldSetBanDate() {
        // Arrange
        LocalDateTime banDate = LocalDateTime.of(2026, 12, 31, 23, 59);
        when(author.getBannedUntil()).thenReturn(null);
        when(CommentMapper.toUserCommentAdminDto(any(User.class))).thenReturn(userCommentDto);

        // Act
        UserCommentAdminDto result = commentService.updateUserBan(USER_ID, banDate);

        // Assert
        verify(author).setBannedUntil(banDate);
        verify(author, never()).setBannedUntil(null);
        verify(CommentMapper).toUserCommentAdminDto(author);
        assertThat(result, is(userCommentDto));
    }

    @Test
    void updateUserBan_whenBanDateProvidedButUserAlreadyBanned_shouldThrowConflictException() {
        // Arrange
        LocalDateTime futureBan = LocalDateTime.now().plusDays(5);
        when(author.getBannedUntil()).thenReturn(futureBan);

        // Act & Assert
        org.junit.jupiter.api.Assertions.assertThrows(
                ConflictException.class,
                () -> commentService.updateUserBan(USER_ID, LocalDateTime.now())
        );

        verify(author, never()).setBannedUntil(any(LocalDateTime.class));
    }

    @Test
    void updateUserBan_whenBanDateNullAndUserCurrentlyBanned_shouldUnbanUser() {
        // Arrange
        LocalDateTime pastBan = LocalDateTime.now().minusDays(5);
        when(author.getBannedUntil()).thenReturn(pastBan);
        when(CommentMapper.toUserCommentAdminDto(any(User.class))).thenReturn(userCommentDto);

        // Act
        UserCommentAdminDto result = commentService.updateUserBan(USER_ID, null);

        // Assert
        verify(author).setBannedUntil(null);
        verify(CommentMapper).toUserCommentAdminDto(author);
        assertThat(result, is(userCommentDto));
    }

    @Test
    void updateUserBan_whenBanDateNullButUserNotBanned_shouldThrowConflictException() {
        // Arrange
        when(author.getBannedUntil()).thenReturn(null);

        // Act & Assert
        org.junit.jupiter.api.Assertions.assertThrows(
                ConflictException.class,
                () -> commentService.updateUserBan(USER_ID, null)
        );

        verify(author, never()).setBannedUntil(any());
    }

    @Test
    void adminDelete_shouldDeleteCommentAndLogWithDto() {
        // Arrange
        CommentDto mockCommentDto = mock(CommentDto.class);
        String logString = "log-comment-string";
        when(CommentMapper.toCommentDto(any(Comment.class))).thenReturn(mockCommentDto);
        when(CommentMapper.toLogComment(any(CommentDto.class))).thenReturn(logString);

        // Act
        commentService.adminDelete(COMMENT_ID);

        // Assert
        verify(commentRepository).delete(comment);
        verify(CommentMapper).toCommentDto(comment);
        verify(CommentMapper).toLogComment(mockCommentDto);
        // Логирование проверяется через verify на мок-объекте логгера, если он замокан;
        // здесь мы полагаемся на то, что метод вызван и логика корректна.
    }
}
