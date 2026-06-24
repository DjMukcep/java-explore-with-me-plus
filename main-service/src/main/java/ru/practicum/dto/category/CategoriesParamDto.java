package ru.practicum.dto.category;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoriesParamDto {
    @PositiveOrZero(message = "can't be negative")
    @Builder.Default
    private int from = 0;

    @Positive(message = "must be greater than 0")
    @Builder.Default
    private int size = 10;
}
