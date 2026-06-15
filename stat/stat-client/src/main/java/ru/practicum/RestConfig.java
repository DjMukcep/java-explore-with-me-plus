package ru.practicum;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestConfig {

    @Bean
    RestClient restClient() {
        return RestClient.builder()
                .baseUrl("http://localhost:9090")
                .build();
    }
}