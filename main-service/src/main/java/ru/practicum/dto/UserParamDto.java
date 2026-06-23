package ru.practicum.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.util.List;

@Data
public class UserParamDto {
    private List<Integer> ids;

    @PositiveOrZero(message = "can't be negative")
    private int from = 0;

    @Positive(message = "must be greater than 0")
    private int size = 10;
}
