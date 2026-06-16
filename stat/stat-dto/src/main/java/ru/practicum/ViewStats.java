package ru.practicum;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ViewStats {

    @NotBlank
    private String app;

    @NotBlank
    private String uri;

    @Min(value = 0)
    @NotNull
    private Long hits;
}
