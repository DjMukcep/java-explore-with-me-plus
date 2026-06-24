package ru.practicum.dto.category;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoriesParamDto {
    @PositiveOrZero(message = "can't be negative")
    private int from;

    @Positive(message = "must be greater than 0")
    private int size;
}
