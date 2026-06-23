package ru.practicum.dto.category;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
@Builder
public class CategoryDto {
    private Long id;
    @NotBlank
    @Length(min = 1, max = 50)
    private String name;
}
