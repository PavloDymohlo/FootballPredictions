package ua.dymohlo.FootballPredictions.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import ua.dymohlo.FootballPredictions.DTO.RegisterDto;
import ua.dymohlo.FootballPredictions.DTO.LoginInDto;
import ua.dymohlo.FootballPredictions.Entity.User;
import ua.dymohlo.FootballPredictions.configuration.PasswordEncoderConfig;
import ua.dymohlo.FootballPredictions.repository.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final CacheManager cacheManager;
    private final MatchService matchService;

    public User register(RegisterDto registerDto) {
        long startCount = 0;
        long userRankingPosition = calculateUserRankingPosition();
        String passwordEncoded = PasswordEncoderConfig.encoderPassword(registerDto.getPassword());
        if (userRepository.findByUserName(registerDto.getUserName()).isPresent()) {
            throw new IllegalArgumentException("This name is already taken");
        }
        User user = User.builder()
                .userName(registerDto.getUserName())
                .password(passwordEncoded)
                .rankingPosition(userRankingPosition)
                .trophyCount(startCount)
                .monthlyScore(startCount)
                .totalScore(startCount)
                .build();
        return userRepository.save(user);
    }

    private long calculateUserRankingPosition() {
        return userRepository.count() + 1;
    }

    public String loginIn(LoginInDto userLoginInDto) {
        Optional<User> user = userRepository.findByUserName(userLoginInDto.getUserName());
        if (user.isEmpty()) {
            throw new NoSuchElementException("Invalid login");
        }
        if (!PasswordEncoderConfig.checkPassword(userLoginInDto.getPassword(), user.get().getPassword())) {
            throw new IllegalArgumentException("Invalid password");
        }
        return "Success";
    }

    public void countUsersPredictionsResult() {
        List<User> users = userRepository.findAll();
        String date = "2024-09-15";
        users.stream()
                .map(user -> {
                    return matchService.compareUsersPredictions(user.getUserName(), date);
                })
                .forEach(predictions -> {
                });
    }

    public List<User> rankingPosition() {
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
        System.out.println(users);
        return users;
    }
}
