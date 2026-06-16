package ru.practicum;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ViewStatsSerializationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testSerialization() throws Exception {
        ViewStats dto = new ViewStats();

        dto.setApp("app");
        dto.setHits(10L);
        dto.setUri("/api/uri");

        String json = objectMapper.writeValueAsString(dto);

        assertThat(json, containsString("\"app\":\"app\""));
        assertThat(json, containsString("\"hits\":10"));
        assertThat(json, containsString("\"uri\":\"/api/uri\""));
    }

    @Test
    void testDeserialization() throws Exception {
        String json = "{\"app\":\"app\", \"hits\":10, \"uri\":\"/api/uri\"}";

        ViewStats dto = objectMapper.readValue(json, ViewStats.class);

        assertThat(dto.getApp(), equalTo("app"));
        assertThat(dto.getHits(), equalTo(10L));
        assertThat(dto.getUri(), equalTo("/api/uri"));
    }
}
