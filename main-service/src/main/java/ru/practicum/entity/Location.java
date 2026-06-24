package ru.practicum.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

@Embeddable
@Data
public class Location {

    @Column(nullable = false)
    private Float lat;
    @Column(nullable = false)
    private Float lon;
}
