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
    private final MatchService matchService;
    private final UserService userService;

        //@Scheduled(cron = "0 0 1 * * *", zone = "Europe/Kiev")
    @Scheduled(cron = "0 44 23 * * *", zone = "Europe/Kiev")
    public void addNewEvents() {
        matchService.getFutureMatches();
    }
    @Scheduled(cron = "0 38 23 * * *", zone = "Europe/Kiev")
    public  void checkUserPredictionsResult(){
        userService.countUsersPredictionsResult();
    }
    @Scheduled(cron = "0 04 00 * * *", zone = "Europe/Kiev")
    public void checkUsersRankingPosition(){
        userService.rankingPosition();
    }
}
