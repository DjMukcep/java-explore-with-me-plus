package ru.practicum;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Collections;
import java.util.List;

@Component
public class StatClient {

    private final RestClient restClient;

    public StatClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public void hit(EndpointHit endpointHit) {
        try {
            restClient.post()
                    .uri("/hit")
                    .body(endpointHit)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            System.out.println("Stats service unavailable: " + e.getMessage());
        }
    }

    public List<ViewStats> getViewStats(StatsRequest statsRequest) {
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/stats")
                            .queryParam("start", statsRequest.getStartTime())
                            .queryParam("end", statsRequest.getEndTime())
                            .queryParam("uris", statsRequest.getUris())
                            .queryParam("unique", statsRequest.isUnique())
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<ViewStats>>() {
                    });
        } catch (RestClientException e) {
            System.out.println("Stats service unavailable: " + e.getMessage());
            return  Collections.emptyList();
        }
    }
}