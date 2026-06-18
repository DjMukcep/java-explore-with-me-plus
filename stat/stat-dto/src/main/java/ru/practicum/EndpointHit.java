package ru.practicum;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EndpointHit {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Positive
    private Long id;

    @NotBlank(message = "Can't be empty")
    private String app;

    @NotBlank(message = "Can't be empty")
    private String uri;

    @NotBlank(message = "Can't be empty")
    private String ip;

    @NotBlank(message = "can't be empty")
    private String timestamp;
}
