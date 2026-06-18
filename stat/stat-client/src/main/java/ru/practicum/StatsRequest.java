package ru.practicum;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class StatsRequest {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<String> uris;
    private Boolean unique;

    public boolean isUnique() {
        return unique != null && unique;
    }
}
