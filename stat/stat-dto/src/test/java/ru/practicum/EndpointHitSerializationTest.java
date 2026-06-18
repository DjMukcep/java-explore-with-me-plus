package ru.practicum;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

public class EndpointHitSerializationTest {

    private static ObjectMapper objectMapper;

    private static final long ID = 10;
    private static final String APP = "some-app";
    private static final String IP = "109.11.31.32";
    private static final String URI = "/api/uri";
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
        EndpointHit endpointHit = EndpointHit.builder()
                .id(ID)
                .app(APP)
                .uri(URI)
                .ip(IP)
                .timestamp(TIMESTAMP)
                .build();

        String json = objectMapper.writeValueAsString(endpointHit);

        assertThat(json, containsString("\"id\":10"));
        assertThat(json, containsString("\"app\":\"some-app\""));
        assertThat(json, containsString("\"ip\":\"109.11.31.32\""));
        assertThat(json, containsString("\"uri\":\"/api/uri\""));
        assertThat(json, containsString("\"timestamp\":\"" + TIMESTAMP_STRING + "\""));

    }

    @Test
    void testDeserialize() throws Exception {
        String json = "{\"id\":10," +
                "\"app\":\"some-app\"," +
                "\"ip\":\"109.11.31.32\"," +
                "\"uri\":\"/api/uri\"," +
                "\"timestamp\":\"" + TIMESTAMP_STRING + "\"}";

        EndpointHit dto = objectMapper.readValue(json, EndpointHit.class);

        assertThat(dto.getId(), equalTo(ID));
        assertThat(dto.getApp(), equalTo(APP));
        assertThat(dto.getIp(), equalTo(IP));
        assertThat(dto.getUri(), equalTo(URI));
        assertThat(dto.getTimestamp(), equalTo(TIMESTAMP));
    }

}
