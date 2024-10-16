package ua.dymohlo.FootballPredictions.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ua.dymohlo.FootballPredictions.service.MatchService;
import ua.dymohlo.FootballPredictions.service.UserService;


@Component
@RequiredArgsConstructor
@Slf4j
public class UpdateDataSchedule {
    private final UserService userService;
    private final MatchService matchService;

    @Scheduled(cron = "0 20 09 * * *", zone = "Europe/Kiev")
    public void getFutureMatches() {
        matchService.getFutureMatches();
    }

    @Scheduled(cron = "0 22 09 * * *", zone = "Europe/Kiev")
    public void getMatchesResultFromApi() {
        matchService.getMatchesResultFromApi();
    }

    @Scheduled(cron = "0 23 09 * * *", zone = "Europe/Kiev")
    public void countUsersPredictionsResult() {
        userService.countUsersPredictionsResult();
    }

    @Scheduled(cron = "0 24 09 * * *", zone = "Europe/Kiev")
    public void rankingPosition() {
        userService.rankingPosition();
    }

    @Scheduled(cron = "0 25 09 L * ?", zone = "Europe/Kiev")
    public void userTrophyCount() {
        userService.userTrophyCount();
    }
}