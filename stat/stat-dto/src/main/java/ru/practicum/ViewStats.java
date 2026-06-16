package ru.practicum;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ViewStats {

    @NotBlank
    private String app;

    @NotBlank
    private String uri;

    @Positive
    @NotNull
    private Long hits;
}
