package ru.practicum.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.user.UserShortDto;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventShortDto {

    private Long id;
    private String title;
    private String annotation;
    private CategoryDto category;
    private String eventDate;
    private Boolean paid;
    private UserShortDto initiator;
    private Boolean requestModeration;
    private String state;
    //private Long confirmedRequests;
    private Long views;
}
