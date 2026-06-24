package ru.practicum.dto.category;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewCategoryDto {
    @NotBlank(message = "Имя не может быть пустым!")
    @Length(min = 1, max = 50, message = "Длина имени должна быть от 1 до 50 символов!")
    private String name;
}

