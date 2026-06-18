package ru.practicum.params;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class GetStatsParams {

    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    @NotNull(message = "Can't be null")
    @JsonFormat(pattern = DATE_TIME_PATTERN, shape = JsonFormat.Shape.STRING, timezone = "UTC")
    private LocalDateTime start;
    @NotNull(message = "Can't be null")
    @JsonFormat(pattern = DATE_TIME_PATTERN, shape = JsonFormat.Shape.STRING, timezone = "UTC")
    private LocalDateTime end;
    private List<String> uris;
    private Boolean unique;
}
