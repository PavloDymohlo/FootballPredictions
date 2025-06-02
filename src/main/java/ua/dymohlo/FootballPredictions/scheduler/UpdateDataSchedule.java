package ua.dymohlo.FootballPredictions.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ua.dymohlo.FootballPredictions.api.PredictionService;
import ua.dymohlo.FootballPredictions.api.UserRankingService;
import ua.dymohlo.FootballPredictions.service.MatchServiceImpl;

@Component
@RequiredArgsConstructor
@Slf4j
public class UpdateDataSchedule {
    private final UserRankingService userRankingService;
    private final PredictionService predictionService;
    private final MatchServiceImpl matchServiceImpl;

    @Scheduled(cron = "0 21 09 * * *", zone = "Europe/Kiev")
    public void getFutureMatches() {
        matchServiceImpl.getFutureMatches();
    }

    @Scheduled(cron = "0 52 08 * * *", zone = "Europe/Kiev")
    public void getMatchesResultFromApi() {
        matchServiceImpl.getMatchesResultFromApi();
    }

    @Scheduled(cron = "0 02 07 * * *", zone = "Europe/Kiev")
    public void countUsersPredictionsResult() {
        predictionService.countAllUsersPredictionsResult();
    }
    @Scheduled(cron = "0 03 07 * * *", zone = "Europe/Kiev")
    public void rankingPosition() {
        userRankingService.updateRankingPositions();
    }

    @Scheduled(cron = "0 04 07 1 * ?", zone = "Europe/Kiev")
    public void userTrophyCount() {
        userRankingService.updateTrophyCounts();
    }

    @Scheduled(cron = "0 05 07 * * *", zone = "Europe/Kiev")
    public void findPassiveUser() {
        userRankingService.removeInactiveUsers();
    }
}
