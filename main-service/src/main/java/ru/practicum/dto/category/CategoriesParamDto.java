package ru.practicum.dto.category;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class CategoriesParamDto {
    @PositiveOrZero(message = "can't be negative")
    private int from = 0;

    @Positive(message = "must be greater than 0")
    private int size = 10;
}
