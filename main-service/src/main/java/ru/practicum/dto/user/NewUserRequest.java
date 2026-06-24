package ru.practicum.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewUserRequest {

    @NotBlank(message = "can't be empty.")
    @Length(min = 6, max = 254, message = "length must be > 5 and < 255")
    @Email
    private String email;

    @NotBlank(message = "can't be empty.")
    @Length(min = 2, max = 250, message = "length must be > 1 and < 251")
    private String name;
}