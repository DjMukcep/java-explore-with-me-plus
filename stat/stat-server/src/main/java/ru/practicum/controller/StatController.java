package ru.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.EndpointHit;
import ru.practicum.ViewStats;
import ru.practicum.params.GetStatsParams;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class StatController {

    // TODO: Implement service


    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public EndpointHit hit(@RequestBody @Valid EndpointHit endpointHit) {
        return null;
    }

    @GetMapping("/stats")
    public List<ViewStats> stats(@ModelAttribute @Valid GetStatsParams params) {

        if (params.getUnique() == null) {
            params.setUnique(false);
        }

        return List.of();
    }
}
