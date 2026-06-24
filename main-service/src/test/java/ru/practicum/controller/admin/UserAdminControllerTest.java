package ru.practicum.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.dto.NewUserRequest;
import ru.practicum.dto.UserDto;
import ru.practicum.dto.UserParamDto;
import ru.practicum.entity.user.UserService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(UserAdminController.class)
class UserAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    void saveUser_shouldReturnCreatedUser() throws Exception {
        NewUserRequest request = NewUserRequest.builder()
                .email("roman@mail.com")
                .name("Roman")
                .build();

        UserDto response = UserDto.builder()
                .id(1L)
                .email("roman@mail.com")
                .name("Roman")
                .build();

        when(userService.saveUser(any(NewUserRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("roman@mail.com"))
                .andExpect(jsonPath("$.name").value("Roman"));

        verify(userService).saveUser(any(NewUserRequest.class));
    }

    @Test
    void getUsers_shouldReturnUsersList() throws Exception {
        List<UserDto> users = List.of(
                UserDto.builder()
                        .id(1L)
                        .email("roman@mail.com")
                        .name("Roman")
                        .build(),
                UserDto.builder()
                        .id(2L)
                        .email("ivan@mail.com")
                        .name("Ivan")
                        .build()
        );

        when(userService.getUsers(any(UserParamDto.class)))
                .thenReturn(users);

        mockMvc.perform(get("/admin/users")
                        .param("ids", "1", "2")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].email").value("roman@mail.com"))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(userService).getUsers(any(UserParamDto.class));
    }

    @Test
    void removeUser_shouldReturnNoContent() throws Exception {
        doNothing().when(userService).deleteUserById(1L);

        mockMvc.perform(delete("/admin/users/1"))
                .andExpect(status().isNoContent());

        verify(userService).deleteUserById(1L);
    }

    @Test
    void saveUser_shouldReturnBadRequest_whenEmailInvalid() throws Exception {
        NewUserRequest request = NewUserRequest.builder()
                .email("bad-email")
                .name("Roman")
                .build();

        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    void getUsers_shouldReturnBadRequest_whenSizeNegative() throws Exception {
        mockMvc.perform(get("/admin/users")
                        .param("size", "-1"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

}