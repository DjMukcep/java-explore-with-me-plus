package ru.practicum;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
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
    private Long id;

    @NotBlank(message = "can't be empty")
    private String app;

    @NotBlank(message = "can't be empty")
    private String uri;

    @NotBlank(message = "can't be empty")
    private String ip;

    @NotBlank(message = "can't be empty")
    private String timestamp;
}
