package ua.dymohlo.FootballPredictions.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ua.dymohlo.FootballPredictions.Entity.User;
import ua.dymohlo.FootballPredictions.api.UserRankingService;
import ua.dymohlo.FootballPredictions.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserRankingServiceImpl implements UserRankingService {
    private final UserRepository userRepository;

    @Override
    public List<User> updateRankingPositions() {
        List<User> users = userRepository.findAll();
        Collections.sort(users, (u1, u2) -> {
            int totalScoreComparison = Long.compare(u2.getTotalScore(), u1.getTotalScore());
            if (totalScoreComparison != 0) {
                return totalScoreComparison;
            }
            int trophyCountComparison = Long.compare(u2.getTrophyCount(), u1.getTrophyCount());
            if (trophyCountComparison != 0) {
                return trophyCountComparison;
            }
            return Long.compare(u2.getMonthlyScore(), u1.getMonthlyScore());
        });

        long currentRank = 1;
        long sameRankCount = 0;
        for (int i = 0; i < users.size(); i++) {
            User currentUser = users.get(i);
            if (i > 0) {
                User previousUser = users.get(i - 1);
                if (currentUser.getTotalScore() == previousUser.getTotalScore() &&
                        currentUser.getTrophyCount() == previousUser.getTrophyCount() &&
                        currentUser.getMonthlyScore() == previousUser.getMonthlyScore()) {
                    sameRankCount++;
                } else {
                    currentRank += sameRankCount + 1;
                    sameRankCount = 0;
                }
            }
            currentUser.setRankingPosition(currentRank);
        }

        userRepository.saveAll(users);
        log.info("All users are arranged according to their ranking positions.");
        return users;
    }

    @Override
    public List<User> updateTrophyCounts() {
        List<User> users = userRepository.findAll();
        OptionalLong maxMonthlyScoreOpt = users.stream()
                .mapToLong(User::getMonthlyScore)
                .max();

        if (maxMonthlyScoreOpt.isEmpty()) {
            return users;
        }

        long maxMonthlyScore = maxMonthlyScoreOpt.getAsLong();
        List<User> usersWithMaxScore = users.stream()
                .filter(user -> user.getMonthlyScore() == maxMonthlyScore)
                .collect(Collectors.toList());

        if (usersWithMaxScore.size() == 1) {
            User topUser = usersWithMaxScore.get(0);
            topUser.setTrophyCount(topUser.getTrophyCount() + 1);
            userRepository.save(topUser);
        }

        resetUserMonthlyScore(users);
        return users;
    }

    @Override
    public void removeInactiveUsers() {
        List<User> users = userRepository.findAll();
        LocalDateTime expiredDate = LocalDateTime.now().minusDays(90);
        users.stream()
                .filter(user -> user.getLastPredictions() != null && user.getLastPredictions().isBefore(expiredDate))
                .forEach(user -> deleteUser(user.getUserName()));
        log.info("Search for passive users has taken place.");
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    private List<User> resetUserMonthlyScore(List<User> users) {
        long startMonthPoint = 0;
        List<User> resetUserMonthlyScore = users.stream()
                .peek(user -> user.setMonthlyScore(startMonthPoint))
                .collect(Collectors.toList());
        userRepository.saveAll(resetUserMonthlyScore);
        return resetUserMonthlyScore;
    }

    private void deleteUser(String username) {
        Optional<User> user = userRepository.findByUserName(username);
        user.ifPresent(userRepository::delete);
        log.info("Delete user with username: " + username);
    }
}