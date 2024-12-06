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

    @Scheduled(cron = "0 30 13 * * *", zone = "Europe/Kiev")
    public void getFutureMatches() {
        matchService.getFutureMatches();
    }

    @Scheduled(cron = "0 31 13 * * *", zone = "Europe/Kiev")
    public void getMatchesResultFromApi() {
        matchService.getMatchesResultFromApi();
    }

    @Scheduled(cron = "0 32 13 * * *", zone = "Europe/Kiev")
    public void countUsersPredictionsResult() {
        userService.countUsersPredictionsResult();
    }

    @Scheduled(cron = "0 33 13 * * *", zone = "Europe/Kiev")
    public void rankingPosition() {
        userService.rankingPosition();
    }

    @Scheduled(cron = "0 34 13 1 * ?", zone = "Europe/Kiev")
    public void userTrophyCount() {
        userService.userTrophyCount();
    }
    @Scheduled(cron = "0 35 13 * * *", zone = "Europe/Kiev")
    public void findPassiveUser() {
        userService.findPassiveUser();
    }
}