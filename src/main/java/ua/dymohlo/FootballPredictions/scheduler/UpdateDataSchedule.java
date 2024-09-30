package ua.dymohlo.FootballPredictions.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ua.dymohlo.FootballPredictions.service.MatchService;
import ua.dymohlo.FootballPredictions.service.UserService;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class UpdateDataSchedule {
    private final UserService userService;
    private final MatchService matchService;

        //@Scheduled(cron = "0 0 1 * * *", zone = "Europe/Kiev")
    @Scheduled(cron = "0 40 22 * * *", zone = "Europe/Kiev")
    public void getFutureMatches() {
        matchService.getFutureMatches();
    }

    @Scheduled(cron = "0 17 12 * * *", zone = "Europe/Kiev")
    public void getMatchesResultFromApi() {
        matchService.getMatchesResultFromApi();
    }
    @Scheduled(cron = "0 22 21 * * *", zone = "Europe/Kiev")
    public  void countUsersPredictionsResult(){
        userService.countUsersPredictionsResult();
    }
    @Scheduled(cron = "0 23 21 * * *", zone = "Europe/Kiev")
    public void rankingPosition(){
        userService.rankingPosition();
    }
}
