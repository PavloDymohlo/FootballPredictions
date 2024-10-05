package ua.dymohlo.FootballPredictions.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.dymohlo.FootballPredictions.DTO.PredictionDTO;
import ua.dymohlo.FootballPredictions.Entity.User;
import ua.dymohlo.FootballPredictions.service.UserService;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/user")
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

//    @GetMapping("/match-status")
//    public ResponseEntity<List<Object>> getAllMatchesWithPredictionStatus(@RequestHeader("userName") String userName,
//                                                                          @RequestParam("date") String date) {
//        List<Object> matchesWithStatus = userService.getAllMatchesWithPredictionStatus(userName, date);
//        if (matchesWithStatus.isEmpty()) {
//            log.info("getAllMatchesWithPredictionStatus noContent");
//            return ResponseEntity.noContent().build();
//        } else {
//            log.info("getAllMatchesWithPredictionStatus isContent");
//            return ResponseEntity.ok(matchesWithStatus);
//        }
//    }

//    @GetMapping("/match-status")
//    public ResponseEntity<List<Object>> getAllMatchesWithPredictionStatus(@RequestHeader("userName") String userName, @RequestParam("date") String date) {
//        List<Object> matchesWithStatus = userService.getAllMatchesWithPredictionStatus(userName, date);
//        if (matchesWithStatus == null || matchesWithStatus.isEmpty()) {
//            log.info("getAllMatchesWithPredictionStatus noContent");
//            return ResponseEntity.noContent().build();
//        } else {
//            log.info("getAllMatchesWithPredictionStatus isContent");
//            return ResponseEntity.ok(matchesWithStatus);
//        }
//    }
//

    @GetMapping("/match-status")
    public List<Object> getAllMatchesWithPredictionStatus(@RequestHeader("userName") String userName,
                                                          @RequestParam("date") String date) {
        return Collections.singletonList(userService.getAllMatchesWithPredictionStatus(userName, date));
    }

}
