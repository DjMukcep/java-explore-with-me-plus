package ru.practicum.entity.comment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.NewCommentDto;
import ru.practicum.dto.comment.UpdateCommentDto;
import ru.practicum.dto.comment.UserCommentAdminDto;
import ru.practicum.entity.event.Event;
import ru.practicum.entity.event.EventService;
import ru.practicum.entity.event.EventState;
import ru.practicum.entity.user.CommentsRank;
import ru.practicum.entity.user.User;
import ru.practicum.entity.user.UserService;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private UserService userService;
    @Mock
    private EventService eventService;

    @InjectMocks
    private CommentServiceImpl commentService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private User testUser;
    private Event testEvent;
    private Comment testComment;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setCommentsCount(0);
        testUser.setRank(CommentsRank.NOVICE);
        testUser.setBannedUntil(null);
        testUser.setAdminWarnings(0);

        testEvent = new Event();
        testEvent.setId(10L);
        testEvent.setState(EventState.PUBLISHED);

        testComment = new Comment();
        testComment.setId(20L);
        testComment.setAdminWarning(false);
        testComment.setAuthor(testUser);
        testComment.setEvent(testEvent);
        testComment.setCreated(LocalDateTime.now().minusDays(10));
        testComment.setUpdated(null);
    }

    @Test
    void createComment_whenValid_thenSuccessAndChangesRank() {
        testUser.setCommentsCount(1);
        NewCommentDto newCommentDto = new NewCommentDto("Текст", 10L);

        Mockito.when(userService.findById(1L)).thenReturn(testUser);
        Mockito.when(eventService.findEventByIdAndState(10L, EventState.PUBLISHED)).thenReturn(testEvent);

        Mockito.when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment c = invocation.getArgument(0);
            c.setId(100L);
            c.setCreated(LocalDateTime.now());
            return c;
        });

        CommentDto result = commentService.createComment(newCommentDto, 1L);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals("Текст", result.getText());
        assertEquals(2, testUser.getCommentsCount());

        assertEquals(CommentsRank.REGULAR, testUser.getRank());
    }

    @Test
    void createComment_whenUserIsBanned_thenThrowsConflictException() {
        // Ставим бан в будущем времени
        testUser.setBannedUntil(LocalDateTime.now().plusDays(1));
        NewCommentDto newCommentDto = new NewCommentDto("Текст", 10L);

        Mockito.when(userService.findById(1L)).thenReturn(testUser);

        assertThrows(ConflictException.class, () -> commentService.createComment(newCommentDto, 1L));
        Mockito.verify(commentRepository, Mockito.never()).save(any());
    }

    @Test
    void createComment_whenBanExpired_thenResetsBanAndSuccess() {
        testUser.setBannedUntil(LocalDateTime.now().minusDays(1));
        NewCommentDto newCommentDto = new NewCommentDto("Текст", 10L);

        Mockito.when(userService.findById(1L)).thenReturn(testUser);
        Mockito.when(eventService.findEventByIdAndState(10L, EventState.PUBLISHED)).thenReturn(testEvent);
        Mockito.when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment c = invocation.getArgument(0);
            c.setCreated(LocalDateTime.now());
            return c;
        });

        assertDoesNotThrow(() -> commentService.createComment(newCommentDto, 1L));
        assertNull(testUser.getBannedUntil());
    }

    @Test
    void updateComment_whenValid_thenSuccess() {
        Event fakeEvent = new Event();
        fakeEvent.setId(10L);

        Comment existingComment = new Comment();
        existingComment.setId(100L);
        existingComment.setCreated(LocalDateTime.now());
        existingComment.setUpdated(LocalDateTime.now());
        existingComment.setText("Старый текст");
        existingComment.setAuthor(testUser);
        existingComment.setEvent(fakeEvent);

        UpdateCommentDto updateDto = new UpdateCommentDto();
        updateDto.setCommentId(100L);
        updateDto.setUserId(1L);
        updateDto.setText("Новый текст");

        Mockito.when(commentRepository.findWithAuthorById(100L)).thenReturn(Optional.of(existingComment));
        Mockito.when(userService.findById(1L)).thenReturn(testUser);

        CommentDto result = commentService.updateComment(updateDto);

        assertNotNull(result);
        assertEquals("Новый текст", result.getText());
        assertEquals("Новый текст", existingComment.getText());
    }

    @Test
    void updateComment_whenNotAuthor_thenThrowsConflictException() {
        // 1. Создаем автора оригинального комментария (Юзер 1)
        User realAuthor = new User();
        realAuthor.setId(1L);

        Comment existingComment = new Comment();
        existingComment.setId(100L);
        existingComment.setAuthor(realAuthor);

        // 2. Создаем пользователя, который ПЫТАЕТСЯ обновить (Юзер 999)
        User hackerUser = new User();
        hackerUser.setId(999L);
        hackerUser.setBannedUntil(null); // чтобы прошел banCheck

        UpdateCommentDto updateDto = new UpdateCommentDto();
        updateDto.setCommentId(100L);
        updateDto.setUserId(999L);
        updateDto.setText("Новый текст");

        // 3. Настраиваем моки для ОБОИХ шагов до проверки автора
        Mockito.when(commentRepository.findWithAuthorById(100L)).thenReturn(Optional.of(existingComment));
        Mockito.when(userService.findById(999L)).thenReturn(hackerUser); // ИСПРАВЛЕНО: теперь findById не вернет null

        // Ожидаем ошибку "It's not your own comment!"
        assertThrows(ConflictException.class, () -> commentService.updateComment(updateDto));
    }

    @Test
    void deleteComment_whenValid_thenCallsDelete() {
        Comment existingComment = new Comment();
        existingComment.setId(100L);
        existingComment.setAuthor(testUser);

        Mockito.when(commentRepository.findWithAuthorById(100L)).thenReturn(Optional.of(existingComment));

        assertDoesNotThrow(() -> commentService.deleteComment(100L, 1L));
        // Проверяем, что репозиторий действительно вызвал метод delete
        Mockito.verify(commentRepository, Mockito.times(1)).delete(existingComment);
    }

    @Test
    void deleteComment_whenCommentNotFound_thenThrowsNotFoundException() {
        Mockito.when(commentRepository.findWithAuthorById(100L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> commentService.deleteComment(100L, 1L));
    }

    @Test
    void getComments_whenCalled_thenReturnsList() {
        Event fakeEvent = new Event();
        fakeEvent.setId(10L);

        Comment c1 = new Comment();
        c1.setId(100L);
        c1.setAuthor(testUser);
        c1.setEvent(fakeEvent);
        c1.setText("Текст 1");
        c1.setCreated(LocalDateTime.now());

        Comment c2 = new Comment();
        c2.setId(101L);
        c2.setAuthor(testUser);
        c2.setEvent(fakeEvent);
        c2.setText("Текст 2");
        c2.setCreated(LocalDateTime.now());

        Mockito.when(commentRepository.findAllByAuthorId(1L)).thenReturn(List.of(c1, c2));

        List<CommentDto> result = commentService.getComments(1L);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Текст 1", result.get(0).getText());
        assertEquals("Текст 2", result.get(1).getText());
    }

    @Test
    void giveWarning_whenUserNotExceededWarningsLimitAndCommentNotReceivedWarning_thenIncreaseWarningsCount() {
        Mockito.when(commentRepository.findWithAuthorById(20L)).thenReturn(Optional.of(testComment));

        UserCommentAdminDto result = commentService.giveWarning(testComment.getId());

        assertEquals(testUser.getId(), result.getAuthorId());
        assertEquals(1, result.getAdminWarnCount());
        assertTrue(testComment.isAdminWarning());
    }

    @Test
    void giveWarning_whenCommentAlreadyReceivedWarning_thenThrowConflictException() {
        String expectedMessage = String.format("User with id %s already received warning for comment with id %s",
                testUser.getId(), testComment.getId());

        testComment.setAdminWarning(true);
        Mockito.when(commentRepository.findWithAuthorById(20L)).thenReturn(Optional.of(testComment));

        ConflictException exception = assertThrows(ConflictException.class,
                () -> commentService.giveWarning(testComment.getId()));

        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void giveWarning_whenUserExceededWarningsLimit_thenBanUserFor6Months() {
        LocalDateTime expectedBanDate = LocalDateTime.now().plusMonths(6);
        testUser.setAdminWarnings(2);
        Mockito.when(commentRepository.findWithAuthorById(20L)).thenReturn(Optional.of(testComment));

        UserCommentAdminDto result = commentService.giveWarning(testComment.getId());

        assertEquals(0, result.getAdminWarnCount());
        var banDate = LocalDateTime.parse(result.getBannedUntil(), FORMATTER);
        assertEquals(expectedBanDate.getYear(), banDate.getYear());
        assertEquals(expectedBanDate.getMonth(), banDate.getMonth());
        assertEquals(expectedBanDate.getDayOfYear(), banDate.getDayOfYear());
    }

    @Test
    void giveWarning_whenUserExceededWarningsLimitAndUserAlreadyBanned_thenThrowConflictException() {
        String expectedMessage = "User already banned";
        testUser.setAdminWarnings(2);
        testUser.setBannedUntil(LocalDateTime.now().plusMonths(3));
        Mockito.when(commentRepository.findWithAuthorById(20L)).thenReturn(Optional.of(testComment));

        ConflictException exception = assertThrows(ConflictException.class,
                () -> commentService.giveWarning(testComment.getId()));

        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void updateUserBan_whenUserNotBannedAndBanDateNotNull_thenBanUser() {
        LocalDateTime banDate = LocalDateTime.now().plusMonths(3);

        Mockito.when(userService.findById(testUser.getId())).thenReturn(testUser);

        UserCommentAdminDto result = commentService.updateUserBan(testUser.getId(), banDate);

        assertEquals(FORMATTER.format(banDate), result.getBannedUntil());
    }

    @Test
    void updateUserBan_whenUserBannedAndBanDateIsNull_thenUnbanUser() {
        LocalDateTime banDate = LocalDateTime.now().plusMonths(3);
        testUser.setBannedUntil(banDate);
        Mockito.when(userService.findById(testUser.getId())).thenReturn(testUser);

        UserCommentAdminDto result = commentService.updateUserBan(testUser.getId(), null);

        assertNull(result.getBannedUntil());
    }

    @Test
    void updateUserBan_whenUserAlreadyBannedAndBanDateNotNull_thenThrowConflictException() {
        String expectedMessage = "User already banned";
        testUser.setBannedUntil(LocalDateTime.now().plusMonths(7));
        LocalDateTime banDate = LocalDateTime.now().plusMonths(3);

        Mockito.when(userService.findById(testUser.getId())).thenReturn(testUser);

        ConflictException exception = assertThrows(ConflictException.class,
                () -> commentService.updateUserBan(testUser.getId(), banDate));

        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void updateUserBan_whenUserNotBannedAndBanDateIsNull_thenThrowConflictException() {
        String expectedMessage = "User not banned";
        Mockito.when(userService.findById(testUser.getId())).thenReturn(testUser);

        ConflictException exception = assertThrows(ConflictException.class,
                () -> commentService.updateUserBan(testUser.getId(), null));

        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void adminDelete_whenValid_thenCallsDelete() {
        Mockito.when(commentRepository.findWithAuthorById(testComment.getId())).thenReturn(Optional.of(testComment));

        assertDoesNotThrow(() -> commentService.adminDelete(testComment.getId()));

        Mockito.verify(commentRepository, Mockito.times(1)).delete(testComment);
    }

    @Test
    void adminDelete_whenCommentNotFound_thenThrowsNotFoundException() {
        Mockito.when(commentRepository.findWithAuthorById(testComment.getId())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> commentService.adminDelete(testComment.getId()));
    }
}