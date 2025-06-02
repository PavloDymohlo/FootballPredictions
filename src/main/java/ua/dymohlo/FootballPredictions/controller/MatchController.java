package ua.dymohlo.FootballPredictions.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.dymohlo.FootballPredictions.service.MatchServiceImpl;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/matches")
public class MatchController {
    private final MatchServiceImpl matchServiceImpl;

    @GetMapping("/result-event")
    public List<Object> getResultMatches() {
        return matchServiceImpl.getMatchesResultFromApi();
    }
}

