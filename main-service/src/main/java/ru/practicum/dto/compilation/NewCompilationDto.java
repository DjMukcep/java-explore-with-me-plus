package ru.practicum.dto.compilation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewCompilationDto {

    @NotNull(message = "events field cannot be null, use empty array [] if no events")
    private Set<Long> events = new HashSet<>();
    @Builder.Default
    private Boolean pinned = false;

    @NotBlank(message = "can't be empty")
    @Size(min = 1, max = 50, message = "length should be >1 and <50")
    private String title;
}
