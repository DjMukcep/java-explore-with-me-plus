package ru.practicum.dto.user;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.util.List;

@Data
public class UserParamDto {
    private List<Integer> ids;

    @PositiveOrZero
    private int from = 0;

    @Positive
    private int size = 10;
}
