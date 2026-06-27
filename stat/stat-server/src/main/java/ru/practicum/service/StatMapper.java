package ru.practicum.service;

import lombok.experimental.UtilityClass;
import ru.practicum.EndpointHit;
import ru.practicum.exception.ValidationException;
import ru.practicum.model.EndpointHitEntity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@UtilityClass
public class StatMapper {

    public static EndpointHitEntity toHitEntity(EndpointHit endpointHit) {

        if (endpointHit.getTimestamp().isAfter(LocalDateTime.now())) {
            throw new ValidationException("time of visit can't be in the future");
        }

        return EndpointHitEntity.builder()
                .app(endpointHit.getApp())
                .uri(endpointHit.getUri())
                .ip(endpointHit.getIp())
                .timestamp(endpointHit.getTimestamp())
                .build();
    }
}
