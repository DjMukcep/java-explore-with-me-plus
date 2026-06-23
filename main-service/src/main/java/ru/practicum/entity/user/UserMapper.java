package ru.practicum.entity.user;

import lombok.experimental.UtilityClass;
import ru.practicum.dto.NewUserRequest;
import ru.practicum.dto.UserDto;

import java.util.List;

@UtilityClass
public class UserMapper {

    static User toUser(final NewUserRequest newUserRequest) {

        return User.builder()
                .mail(newUserRequest.getEmail())
                .name(newUserRequest.getName())
                .build();
    }

    static UserDto toUserDto(final User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getMail())
                .name(user.getName())
                .build();
    }

    static List<UserDto> toUserDtos(final List<User> users) {
        return users.stream()
                .map(UserMapper::toUserDto)
                .toList();
    }
}
