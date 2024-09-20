package ua.dymohlo.FootballPredictions.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ua.dymohlo.FootballPredictions.DTO.PredictionDTO;
import ua.dymohlo.FootballPredictions.Entity.Match;
import ua.dymohlo.FootballPredictions.service.MatchService;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/matches")
public class MatchController {
    private final MatchService matchService;

    @GetMapping("/future-event")
    public List<String> getFutureMatches() {
        return matchService.getFutureMatches();
    }
    @GetMapping("/result-event")
    public List<String> getResultMatches(){
        return matchService.matchesResult();
    }

    @PostMapping("/predictions")
    public String usersPredictions(@RequestBody PredictionDTO request) {
        matchService.cacheUsersPredictions(request);
        return "Success";
    }

}

