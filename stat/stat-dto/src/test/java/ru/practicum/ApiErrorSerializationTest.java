package ru.practicum;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

public class ApiErrorSerializationTest {

    private static ObjectMapper objectMapper;

    private static final String MESSAGE = "Validation Error";
    private static final String REASON = "Field 'ip' must not be null";
    private static final HttpStatus STATUS = HttpStatus.BAD_REQUEST;
    private static final String ERRORS = "some stack trace";
    private static final LocalDateTime TIMESTAMP = LocalDateTime.of(2000, 3, 12, 13, 31, 14);
    private static final String TIMESTAMP_STRING = "2000-03-12 13:31:14";

    @BeforeAll
    static void init() {
        objectMapper = new ObjectMapper();
        JavaTimeModule javaTimeModule = new JavaTimeModule();

        objectMapper.registerModule(javaTimeModule);
    }

    @Test
    void testSerialize() throws Exception {
        ApiError apiError = new ApiError(MESSAGE, REASON, STATUS, TIMESTAMP, ERRORS);

        String json = objectMapper.writeValueAsString(apiError);

        assertThat(json, containsString("\"message\":\"Validation Error\""));
        assertThat(json, containsString("\"reason\":\"Field 'ip' must not be null\""));
        assertThat(json, containsString("\"httpStatus\":\"BAD_REQUEST\""));
        assertThat(json, containsString("\"timestamp\":\"" + TIMESTAMP_STRING + "\""));
        assertThat(json, containsString("\"errors\":\"some stack trace\""));
    }

    @Test
    void testDeserialize() throws Exception {
        String json = "{\"message\":\"Validation Error\"," +
                "\"reason\":\"Field 'ip' must not be null\"," +
                "\"httpStatus\":\"BAD_REQUEST\"," +
                "\"timestamp\":\"" + TIMESTAMP_STRING + "\"," +
                "\"errors\":\"some stack trace\"}";

        ApiError dto = objectMapper.readValue(json, ApiError.class);

        assertThat(dto.getMessage(), equalTo(MESSAGE));
        assertThat(dto.getReason(), equalTo(REASON));
        assertThat(dto.getHttpStatus(), equalTo(STATUS));
        assertThat(dto.getTimestamp(), equalTo(TIMESTAMP));
        assertThat(dto.getErrors(), equalTo(ERRORS));
    }
}
