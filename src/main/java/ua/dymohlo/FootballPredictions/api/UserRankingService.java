package ua.dymohlo.FootballPredictions.api;

import ua.dymohlo.FootballPredictions.Entity.User;
import java.util.List;

public interface UserRankingService {
    List<User> updateRankingPositions();
    List<User> updateTrophyCounts();
    void removeInactiveUsers();
    List<User> getAllUsers();
}