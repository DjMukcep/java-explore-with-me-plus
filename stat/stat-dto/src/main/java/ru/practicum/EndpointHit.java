package ru.practicum;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
public class EndpointHit {
    @Positive
    private final Long id;

    @NotBlank(message = "Can't be empty")
    private String app;

    @NotBlank(message = "Can't be empty")
    private String uri;

    @NotBlank(message = "Can't be empty")
    private String ip;

    @NotNull(message = "Can't be null")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    @JsonCreator
    public EndpointHit(
            @JsonProperty("id") Long id,
            @JsonProperty("app") String app,
            @JsonProperty("uri") String uri,
            @JsonProperty("ip") String ip,
            @JsonProperty("timestamp") LocalDateTime timestamp) {
        this.id = id;
        this.app = app;
        this.uri = uri;
        this.ip = ip;
        this.timestamp = timestamp;
    }
}
