package ru.practicum.service;

import lombok.experimental.UtilityClass;
import ru.practicum.EndpointHit;
import ru.practicum.exception.ValidationException;
import ru.practicum.model.EndpointHitEntity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@UtilityClass
public class StatMapper {

    private static final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static EndpointHitEntity toHitEntity(EndpointHit endpointHit) {
        LocalDateTime time = LocalDateTime.parse(endpointHit.getTimestamp(), formatter);

        if (time.isAfter(LocalDateTime.now())) {
            throw new ValidationException("time of visit can't be in the future");
        }

        return EndpointHitEntity.builder()
                .app(endpointHit.getApp())
                .uri(endpointHit.getUri())
                .ip(endpointHit.getIp())
                .timestamp(time)
                .build();
    }
}
