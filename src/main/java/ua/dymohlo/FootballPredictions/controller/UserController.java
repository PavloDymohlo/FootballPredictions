package ua.dymohlo.FootballPredictions.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.dymohlo.FootballPredictions.DTO.PredictionDTO;
import ua.dymohlo.FootballPredictions.service.UserService;

import java.util.Collections;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/user")
public class UserController {
    private final UserService userService;

    @PostMapping("/send-predictions")
    public String usersPredictions(@RequestBody PredictionDTO request) {
        userService.cacheUsersPredictions(request);
        return "Success";
    }

    @GetMapping("/get-predictions")
    public ResponseEntity<PredictionDTO> getUsersPredictions(@RequestHeader("userName") String userName,
                                                             @RequestParam("date") String date) {
        PredictionDTO predictions = userService.getUsersPredictions(userName, date);
        if (predictions == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(predictions);
    }

    @GetMapping("/event")
    public List<Object> getFutureMatchesFromCache(@RequestHeader("userName") String userName,
                                                  @RequestParam("date") String date) {
        return Collections.singletonList(userService.getFutureMatchesFromCache(userName, date));
    }

    @GetMapping("/match-status")
    public List<Object> getAllMatchesWithPredictionStatus(@RequestHeader("userName") String userName,
                                                          @RequestParam("date") String date) {
        return Collections.singletonList(userService.getAllMatchesWithPredictionStatus(userName, date));
    }

    @GetMapping("/users")
    public List<Object> allUsersList() {
        return Collections.singletonList(userService.allUsers());
    }
}