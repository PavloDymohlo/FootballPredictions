package ua.dymohlo.FootballPredictions.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import ua.dymohlo.FootballPredictions.DTO.MatchStatusDto;
import ua.dymohlo.FootballPredictions.DTO.PredictionDTO;
import ua.dymohlo.FootballPredictions.Entity.User;
import ua.dymohlo.FootballPredictions.api.PredictionService;
import ua.dymohlo.FootballPredictions.component.MatchParser;
import ua.dymohlo.FootballPredictions.repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PredictionServiceImpl implements PredictionService {
    private final UserRepository userRepository;
//    private final CacheManager cacheManager;
    private final MatchParser matchParser;
    private final RedisTemplate<String, Object> redisTemplate;
    private final MatchServiceImpl matchServiceImpl;

    @Override
    @CachePut(value = "userPredictions", key = "#predictionDTO.userName + '_' + #predictionDTO.matchDate")
    public PredictionDTO saveUserPredictions(PredictionDTO predictionDTO) {
        log.info("Caching predictions for user: {} on date: {}", predictionDTO.getUserName(), predictionDTO.getMatchDate());
        Optional<User> optionalUser = userRepository.findByUserName(predictionDTO.getUserName());
        User user = optionalUser.get();
        long sumNewPredictions = matchParser.countTotalMatches(predictionDTO.getPredictions());
        user.setPredictionCount(user.getPredictionCount() + sumNewPredictions);
        user.setLastPredictions(LocalDateTime.now());
        userRepository.save(user);
        log.info("The number of forecasts for the user " + user.getUserName() + "  increased by " + sumNewPredictions + " forecasts.");
        return predictionDTO;
    }

    @Override
    @Cacheable(value = "userPredictions", key = "#userName + '_' + #matchDate", unless = "#result == null")
    public PredictionDTO getUserPredictions(String userName, String matchDate) {
        String key = "userPredictions::" + userName + "_" + matchDate;
        Object cachedValue = redisTemplate.opsForValue().get(key);
        if (cachedValue instanceof PredictionDTO) {
            System.out.println("for front: " + cachedValue);
            return (PredictionDTO) cachedValue;
        }
        return null;
    }

    @Override
    public List<Object> getCorrectPredictions(String userName, String date) {
        try {
            List<Object> results = matchServiceImpl.getMatchesFromCacheByDate(date);
            PredictionDTO predictions = getUserPredictions(userName, date);
            List<Object> onlyMatchResult = matchesResultParser(results);
            List<Object> userPredictions = userPredictionsParser(predictions);
            List<Object> correctResult = new ArrayList<>();
            int numberOfMatches = Math.min(onlyMatchResult.size(), userPredictions.size());
            for (int i = 0; i < numberOfMatches; i++) {
                Object matchResult = onlyMatchResult.get(i);
                Object userPrediction = userPredictions.get(i);
                if (matchesAreEqual(matchResult, userPrediction)) {
                    correctResult.add(matchResult);
                }
            }
            log.info(correctResult.toString());
            return correctResult;
        } catch (NullPointerException e) {
            String errorMessage = " матчі не відбувалися. Очікуйте наступних результатів.";
            log.error(errorMessage);
            throw new NullPointerException(date + errorMessage);
        }
    }

    @Override
    public List<Object> getAllMatchesWithPredictionStatus(String userName, String date) {
        List<Object> allMatches = matchServiceImpl.getMatchesFromCacheByDate(date);
        List<Object> correctPredictions = getCorrectPredictions(userName, date);
        List<Object> matchesWithStatus = new ArrayList<>();
        Map<String, Object> currentCompetition = null;

        for (Object match : allMatches) {
            if (match instanceof Map && ((Map<?, ?>) match).containsKey("tournament")) {
                currentCompetition = (Map<String, Object>) match;
                matchesWithStatus.add(currentCompetition);
                continue;
            }
            boolean isCorrect = correctPredictions.contains(match);
            matchesWithStatus.add(new MatchStatusDto(match, isCorrect));
        }
        System.out.println(matchesWithStatus);
        return matchesWithStatus;
    }

    @Override
    public void processUserPredictionResults(String userName, String date) {
        Optional<User> optionalUser = userRepository.findByUserName(userName);
        if (!optionalUser.isPresent()) {
            return;
        }
        User user = optionalUser.get();
        List<Object> correctResults = getCorrectPredictions(userName, date);
        int userPoint = correctResults.size();
        user.setMonthlyScore(user.getMonthlyScore() + userPoint);
        user.setTotalScore(user.getTotalScore() + userPoint);
        user.setPercentGuessedMatches(updatePercentGuessedMatches(user));
        userRepository.save(user);
    }

    @Override
    public void countAllUsersPredictionsResult() {
        List<User> users = userRepository.findAll();
        String date = LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("dd/MM"));
        users.forEach(user -> {
            processUserPredictionResults(user.getUserName(), date);
        });
        log.info("All users' predictions results have been calculated.");
    }

    private List<Object> matchesResultParser(List<Object> results) {
        List<Object> onlyMatchResults = new ArrayList<>();
        for (Object result : results) {
            if (!(result instanceof Map)) {
                onlyMatchResults.add(result);
            }
        }
        return onlyMatchResults;
    }

    private List<Object> userPredictionsParser(PredictionDTO predictions) {
        List<Object> userPredictions = new ArrayList<>();
        if (predictions != null) {
            for (Object prediction : predictions.getPredictions()) {
                if (!(prediction instanceof Map)) {
                    userPredictions.add(prediction);
                }
            }
        }
        return userPredictions;
    }

    private boolean matchesAreEqual(Object matchResult, Object userPrediction) {
        if (matchResult instanceof List && userPrediction instanceof List) {
            List<?> matchList = (List<?>) matchResult;
            List<?> predictionList = (List<?>) userPrediction;
            if (matchList.size() == predictionList.size() && matchList.size() == 2) {
                String team1Result = (String) matchList.get(0);
                String team1Prediction = (String) predictionList.get(0);
                String team2Result = (String) matchList.get(1);
                String team2Prediction = (String) predictionList.get(1);
                int team1ResultScore = extractScore(team1Result);
                int team1PredictionScore = extractScore(team1Prediction);
                int team2ResultScore = extractScore(team2Result);
                int team2PredictionScore = extractScore(team2Prediction);
                if (team1ResultScore == -1 || team2ResultScore == -1 ||
                        team1PredictionScore == -1 || team2PredictionScore == -1) {
                    return false;
                }
                return team1ResultScore == team1PredictionScore && team2ResultScore == team2PredictionScore;
            }
        }
        return false;
    }

    private int extractScore(String teamResult) {
        if (teamResult == null || "н/в".equals(teamResult)) {
            return -1;
        }
        int bracketIndex = teamResult.indexOf("(");
        String mainPart = bracketIndex > 0 ? teamResult.substring(0, bracketIndex).trim() : teamResult.trim();
        String[] parts = mainPart.split(" ");
        if (parts.length == 0) {
            return -1;
        }
        try {
            return Integer.parseInt(parts[parts.length - 1]);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private int updatePercentGuessedMatches(User user) {
        long userPredictionCount = user.getPredictionCount();
        return (int) Math.round((double) user.getTotalScore() / userPredictionCount * 100);
    }
}