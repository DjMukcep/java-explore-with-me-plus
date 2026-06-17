package ru.practicum;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@WebMvcTest(StatController.class)
class StatControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);

    private CreateEndpointHit createEndpointHit;
    private LocalDateTime testStartDate;
    private LocalDateTime testEndDate;
    private List<String> testUris;

    @BeforeEach
    void setUp() {
        LocalDateTime testTimestamp = LocalDateTime.of(2023, 10, 1, 12, 0, 0);
        createEndpointHit = new CreateEndpointHit();
        createEndpointHit.setApp("TestApp");
        createEndpointHit.setUri("/test/uri");
        createEndpointHit.setIp("192.168.1.1");
        createEndpointHit.setTimestamp(testTimestamp);

        testStartDate = LocalDateTime.of(2023, 1, 1, 0, 0, 0);
        testEndDate = LocalDateTime.of(2023, 12, 31, 23, 59, 59);
        testUris = List.of("/uri1", "/uri2");
    }

    @Test
    void hit_validInput_shouldReturnCreated() throws Exception {
        mockMvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createEndpointHit)))
                .andExpect(status().isCreated());
    }

    @Test
    void hit_emptyApp_shouldReturnBadRequest() throws Exception {
        createEndpointHit.setApp("");

        mockMvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createEndpointHit)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void hit_nullApp_shouldReturnBadRequest() throws Exception {
        createEndpointHit.setApp(null);

        mockMvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createEndpointHit)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void hit_emptyUri_shouldReturnBadRequest() throws Exception {
        createEndpointHit.setUri("");

        mockMvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createEndpointHit)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void hit_nullUri_shouldReturnBadRequest() throws Exception {
        createEndpointHit.setUri(null);

        mockMvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createEndpointHit)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void hit_emptyIp_shouldReturnBadRequest() throws Exception {
        createEndpointHit.setIp("");

        mockMvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createEndpointHit)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void hit_nullIp_shouldReturnBadRequest() throws Exception {
        createEndpointHit.setIp(null);

        mockMvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createEndpointHit)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void hit_nullTimestamp_shouldReturnBadRequest() throws Exception {
        createEndpointHit.setTimestamp(null);

        mockMvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createEndpointHit)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void hit_wrongFormatTimestamp_shouldReturnBadRequest() throws Exception {
        LocalDateTime testTimestamp = createEndpointHit.getTimestamp();
        String correctTimestampString = testTimestamp.format(FORMATTER);

        String json = objectMapper.writeValueAsString(createEndpointHit);
        json = json.replace(correctTimestampString, testTimestamp.toString());

        mockMvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void stats_WithValidParams_ShouldReturnOk() throws Exception {
        mockMvc.perform(get("/stats")
                        .param("start", testStartDate.format(FORMATTER))
                        .param("end", testEndDate.format(FORMATTER))
                        .param("uris", testUris.toArray(new String[0]))
                        .param("unique", "false")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void stats_WithMissingStartParam_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/stats")
                        .param("end", testEndDate.format(FORMATTER))
                        .param("uris", testUris.toArray(new String[0]))
                        .param("unique", "false")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void stats_WithMissingEndParam_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/stats")
                        .param("start", testStartDate.format(FORMATTER))
                        .param("uris", testUris.toArray(new String[0]))
                        .param("unique", "false")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void stats_WithInvalidDateFormat_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/stats")
                        .param("start", "invalid-date")
                        .param("end", testEndDate.format(FORMATTER))
                        .param("uris", testUris.toArray(new String[0]))
                        .param("unique", "false")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}