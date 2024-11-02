package ua.dymohlo.FootballPredictions.controller;

import lombok.RequiredArgsConstructor;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.dymohlo.FootballPredictions.service.MatchService;
import ua.dymohlo.FootballPredictions.service.UserService;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/matches")
public class MatchController {
    private final UserService userService;
    private final MatchService matchService;

    // method for testing in Postman. Delete after
    @GetMapping("/future-event")
    public List<Object> getFutureMatches() {
        return matchService.getFutureMatches();
    }

//    @GetMapping("/result-event")
//    public List<Object> getResultMatches() {
//        return matchService.getMatchesFromCacheByDate("02/11");
//    }
@GetMapping("/result-event")
public List<Object> getResultMatches() {
    return matchService.getMatchesResultFromApi();
}
}

