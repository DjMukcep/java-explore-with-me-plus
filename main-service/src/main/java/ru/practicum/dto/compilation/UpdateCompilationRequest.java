package ru.practicum.dto.compilation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateCompilationRequest {

    private Set<Long> events;
    private String title;
    private Boolean pinned;
}
