package ua.dymohlo.FootballPredictions.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.dymohlo.FootballPredictions.DTO.PredictionDTO;
import ua.dymohlo.FootballPredictions.api.MatchService;
import ua.dymohlo.FootballPredictions.api.PredictionService;
import ua.dymohlo.FootballPredictions.api.UserRankingService;

import java.util.Collections;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/user")
public class UserController {
    private final PredictionService predictionService;
    private final UserRankingService userRankingService;
    private final MatchService matchService;

    @PostMapping("/send-predictions")
    public String usersPredictions(@RequestBody PredictionDTO request) {
        predictionService.saveUserPredictions(request);
        return "Success";
    }

    @GetMapping("/get-predictions")
    public ResponseEntity<PredictionDTO> getUsersPredictions(@RequestHeader("userName") String userName,
                                                             @RequestParam("date") String date) {
        PredictionDTO predictions = predictionService.getUserPredictions(userName, date);
        if (predictions == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(predictions);
    }

    @GetMapping("/event")
    public List<Object> getFutureMatchesFromCache(@RequestHeader("userName") String userName,
                                                  @RequestParam("date") String date) {
        return Collections.singletonList(matchService.getFutureMatchesFromCache(userName, date));
    }

    @GetMapping("/match-status")
    public List<Object> getAllMatchesWithPredictionStatus(@RequestHeader("userName") String userName,
                                                          @RequestParam("date") String date) {
        return Collections.singletonList(predictionService.getAllMatchesWithPredictionStatus(userName, date));
    }

    @GetMapping("/users")
    public List<Object> allUsersList() {
        return Collections.singletonList(userRankingService.getAllUsers());
    }
}
