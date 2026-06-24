package ru.practicum.entity.user;

import ru.practicum.dto.NewUserRequest;
import ru.practicum.dto.UserDto;
import ru.practicum.dto.UserParamDto;

import java.util.List;

public interface UserService {

    UserDto saveUser(NewUserRequest userRequest);

    List<UserDto> getUsers(UserParamDto userParamDto);

    void deleteUserById(Long userId);

    public User findById(Long userId);
}
