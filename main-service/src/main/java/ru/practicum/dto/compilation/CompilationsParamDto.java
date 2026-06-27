package ru.practicum.dto.compilation;

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
public class CompilationsParamDto {

    @PositiveOrZero(message = "can't be negative")
    private int from = 0;

    @Positive(message = "must be greater than 0")
    private int size = 10;

    private Boolean pinned;
}
