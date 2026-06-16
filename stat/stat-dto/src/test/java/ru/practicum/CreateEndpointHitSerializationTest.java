package ru.practicum;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

public class CreateEndpointHitSerializationTest {

    private static ObjectMapper objectMapper;

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
        CreateEndpointHit createEndpointHit = new CreateEndpointHit();

        createEndpointHit.setApp(APP);
        createEndpointHit.setIp(IP);
        createEndpointHit.setUri(URI);
        createEndpointHit.setTimestamp(TIMESTAMP);

        String json = objectMapper.writeValueAsString(createEndpointHit);

        assertThat(json, containsString("\"app\":\"some-app\""));
        assertThat(json, containsString("\"ip\":\"109.11.31.32\""));
        assertThat(json, containsString("\"uri\":\"/api/uri\""));
        assertThat(json, containsString("\"timestamp\":\"" + TIMESTAMP_STRING + "\""));

    }

    @Test
    void testDeserialize() throws Exception {
        String json = "{\"app\":\"some-app\"," +
                "\"ip\":\"109.11.31.32\"," +
                "\"uri\":\"/api/uri\"," +
                "\"timestamp\":\""+ TIMESTAMP_STRING +"\"}";

        CreateEndpointHit dto = objectMapper.readValue(json, CreateEndpointHit.class);

        assertThat(dto.getApp(), equalTo(APP));
        assertThat(dto.getIp(), equalTo(IP));
        assertThat(dto.getUri(), equalTo(URI));
        assertThat(dto.getTimestamp(), equalTo(TIMESTAMP));
    }
}
