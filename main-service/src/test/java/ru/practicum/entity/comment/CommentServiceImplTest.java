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
import ru.practicum.entity.event.Event;
import ru.practicum.entity.event.EventService;
import ru.practicum.entity.event.EventState;
import ru.practicum.entity.user.CommentsRank;
import ru.practicum.entity.user.User;
import ru.practicum.entity.user.UserService;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;

import java.time.LocalDateTime;
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

    private User testUser;
    private Event testEvent;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setCommentsCount(0);
        testUser.setRank(CommentsRank.NOVICE);
        testUser.setBannedUntil(null);

        testEvent = new Event();
        testEvent.setId(10L);
        testEvent.setState(EventState.PUBLISHED);
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
}