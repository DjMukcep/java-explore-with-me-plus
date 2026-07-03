package ru.practicum.controller.admin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.dto.comment.UserCommentAdminDto;
import ru.practicum.entity.comment.CommentService;

import java.time.format.DateTimeFormatter;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommentAdminController.class)
class CommentAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CommentService commentService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private UserCommentAdminDto userCommentAdminDto;

    private static final long TEST_COMMENT_ID = 1L;
    private static final long TEST_USER_ID = 2L;

    @BeforeEach
    void setUp() {
        userCommentAdminDto = UserCommentAdminDto.builder()
                .authorName("Test Name")
                .authorId(TEST_USER_ID)
                .adminWarnCount(0)
                .bannedUntil(null)
                .build();
    }

    @Test
    void warnUser_returnsOk() throws Exception {
        userCommentAdminDto.setAdminWarnCount(1);

        when(commentService.giveWarning(eq(TEST_COMMENT_ID))).thenReturn(userCommentAdminDto);

        mockMvc.perform(patch("/admin/comments/{commentId}", TEST_COMMENT_ID)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void updateUserBan_withBanDate_returnsOk() throws Exception {
        var banDate = java.time.LocalDateTime.now().plusDays(1);
        userCommentAdminDto.setBannedUntil(banDate.format(FORMATTER));

        when(commentService.updateUserBan(eq(TEST_USER_ID), eq(banDate))).thenReturn(userCommentAdminDto);

        mockMvc.perform(patch("/admin/comments/users/{userId}", TEST_USER_ID)
                                .param("banDate", banDate.format(FORMATTER))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void updateUserBan_withoutBanDate_returnsOk() throws Exception {
        var banDate = java.time.LocalDateTime.now().plusDays(1);
        userCommentAdminDto.setBannedUntil(banDate.format(FORMATTER));

        when(commentService.updateUserBan(eq(TEST_USER_ID), ArgumentMatchers.isNull())).thenReturn(userCommentAdminDto);

        mockMvc.perform(patch("/admin/comments/users/{userId}", TEST_USER_ID)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void deleteComment_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/admin/comments/{commentId}", TEST_COMMENT_ID)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}
