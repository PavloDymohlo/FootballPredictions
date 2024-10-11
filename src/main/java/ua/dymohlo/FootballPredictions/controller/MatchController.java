package ua.dymohlo.FootballPredictions.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ua.dymohlo.FootballPredictions.DTO.PredictionDTO;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.dymohlo.FootballPredictions.service.MatchService;
import ua.dymohlo.FootballPredictions.service.UserService;

import java.util.Collections;
import java.util.List;
import java.util.Map;


@RestController
@RequiredArgsConstructor
@RequestMapping("/matches")
public class MatchController {
    private final UserService userService;
    private final MatchService matchService;

    // method for testing. Delete after
    @GetMapping("/future-event")
    public List<Object> getFutureMatches() {
        return matchService.getFutureMatches();
    }

//    @GetMapping("/future-event")
//    public List<Object> getFutureMatches() {
//        return userService.getAllMatchesWithPredictionStatus("John", "2024-09-28");
//    }




}

