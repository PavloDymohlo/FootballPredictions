package ua.dymohlo.FootballPredictions.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import ua.dymohlo.FootballPredictions.DTO.PredictionDTO;
import ua.dymohlo.FootballPredictions.Entity.Competition;
import ua.dymohlo.FootballPredictions.Entity.Match;
import ua.dymohlo.FootballPredictions.Entity.User;
import ua.dymohlo.FootballPredictions.component.MatchParser;
import ua.dymohlo.FootballPredictions.repository.CompetitionRepository;
import ua.dymohlo.FootballPredictions.repository.MatchRepository;
import ua.dymohlo.FootballPredictions.repository.UserRepository;


import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchService {
    private final WebClient webClient;
    private final MatchParser matchParser;
    private final MatchRepository matchRepository;
    private final ApplicationContext applicationContext;
    private final RedisTemplate<String, Object> redisTemplate;
    private final CompetitionRepository competitionRepository;
    private final UserRepository userRepository;

    @Value("${docs.football-data.url}")
    private String apiUrl;
    @Value("${docs.football-data.key}")
    private String apiKey;

    // do not remove the two bottom methods!!!!
    public List<Object> getFutureMatches() {
        String targetDate = "2024-09-15";
        List<Object> matches2002 = getMatchService().getMatchesForDate("PL", targetDate);
        //List<Object> matchesPL = getMatchService().getMatchesForDate("SA", targetDate);

        List<Object> combinedMatches = new ArrayList<>();
        combinedMatches.addAll(matches2002);
        //combinedMatches.addAll(matchesPL);

        int matchCount = matchParser.countTotalMatches(matches2002);
        updateMatchRepository(matchCount);
        return combinedMatches;
    }


    public List<Object> matchesResult() {
        String targetDate = "2024-09-15";
        List<Object> matches2002 = getMatchService().getMatchesForDate("PL", targetDate);
        //List<Object> matchesPL = getMatchService().getMatchesForDate("SA", targetDate);

        List<Object> combinedMatches = new ArrayList<>();
        combinedMatches.addAll(matches2002);
        //combinedMatches.addAll(matchesPL);

        return combinedMatches;
    }

//    public List<Object> getFutureMatches() {
//        String targetDate = LocalDate.now().plusDays(3).toString();
//        List<Competition> competitions = competitionRepository.findAll();
//        List<String> competitionApiIds = competitions.stream()
//                .map(Competition::getCompetitionApiId)
//                .collect(Collectors.toList());
//        List<Object> combinedMatches = new ArrayList<>();
//        for (String competitionApiId : competitionApiIds) {
//            List<Object> matches = getMatchService().getMatchesForDate(competitionApiId, targetDate);
//            combinedMatches.addAll(matches);
//        }
//        int matchCount = matchParser.countTotalMatches(combinedMatches);
//        updateMatchRepository(matchCount);
//        return combinedMatches;
//    }
//    public List<Object> matchesResult() {
//        String targetDate = LocalDate.now().minusDays(1).toString();
//        List<Competition> competitions = competitionRepository.findAll();
//        List<String> competitionApiIds = competitions.stream()
//                .map(Competition::getCompetitionApiId)
//                .collect(Collectors.toList());
//        List<Object> combinedMatches = new ArrayList<>();
//        for (String competitionApiId : competitionApiIds) {
//            List<Object> matches = getMatchService().getMatchesForDate(competitionApiId, targetDate);
//            combinedMatches.addAll(matches);
//        }
//        return combinedMatches;
//    }

    private MatchService getMatchService() {
        return applicationContext.getBean(MatchService.class);
    }

    @CachePut(value = "matchesCache", key = "#targetDate", condition = "#result != null && !#result.isEmpty()")
    public List<Object> getMatchesForDate(String leagueId, String targetDate) {
        String uri = String
                .format("http://api.football-data.org/v4/competitions/%s/matches?dateFrom=%s&dateTo=%s", leagueId, targetDate, targetDate);
        String response = webClient.get()
                .uri(uri)
                .header("X-Auth-Token", apiKey)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        try {
            List<Object> parsedMatches = matchParser.parseMatches(response);
            List<Object> filteredMatches = filterMatchesWithCompetitions(parsedMatches);
            if (!filteredMatches.isEmpty()) {
                return filteredMatches;
            } else {
                return Collections.emptyList();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Object> filterMatchesWithCompetitions(List<Object> parsedMatches) {
        List<Object> filteredMatches = new ArrayList<>();
        Map<String, String> currentCompetition = null;
        List<List<String>> currentMatches = new ArrayList<>();
        for (Object obj : parsedMatches) {
            if (obj instanceof Map) {
                if (currentCompetition != null && !currentMatches.isEmpty()) {
                    filteredMatches.add(currentCompetition);
                    filteredMatches.addAll(currentMatches);
                }
                currentCompetition = (Map<String, String>) obj;
                currentMatches.clear();
            } else if (obj instanceof List) {
                currentMatches.add((List<String>) obj);
            }
        }
        if (currentCompetition != null && !currentMatches.isEmpty()) {
            filteredMatches.add(currentCompetition);
            filteredMatches.addAll(currentMatches);
        }
        return filteredMatches;
    }

    private void updateMatchRepository(int matchCount) {
        String matchName = "allCompetition";
        Optional<Match> match = matchRepository.findByCompetitionName(matchName);
        match.ifPresent(m -> {
            m.setTotalMatches(m.getTotalMatches() + matchCount);
            matchRepository.save(m);
        });
    }

    @CachePut(value = "userPredictions", key = "#predictionDTO.userName + '_' + #predictionDTO.matchDate")
    public PredictionDTO cacheUsersPredictions(PredictionDTO predictionDTO) {
        log.info("Caching predictions for user: {} on date: {}", predictionDTO.getUserName(), predictionDTO.getMatchDate());
        return predictionDTO;
    }

    @Cacheable(value = "userPredictions", key = "#userName + '_' + #matchDate")
    public PredictionDTO getUsersPredictions(String userName, String matchDate) {
        String key = "userPredictions::" + userName + "_" + matchDate;
        Object cachedValue = redisTemplate.opsForValue().get(key);
        if (cachedValue instanceof PredictionDTO) {
            return (PredictionDTO) cachedValue;
        }
        return null;
    }

    public List<Object> compareUsersPredictions(String userName, String date) {
        Optional<User> optionalUser = userRepository.findByUserName(userName);
        if (!optionalUser.isPresent()) {
            return Collections.emptyList();
        }
        User user = optionalUser.get();
        List<Object> results = matchesResult();
        PredictionDTO predictions = getUsersPredictions(userName, date);
        List<Object> correctResult = new ArrayList<>();
        List<Object> onlyMatchResult = matchesResultParser(results);
        List<Object> userPredictions = userPredictionsParser(predictions);
        int numberOfMatches = Math.min(onlyMatchResult.size(), userPredictions.size());

        int userPoint = 0;
        for (int i = 0; i < numberOfMatches; i++) {
            Object matchResult = onlyMatchResult.get(i);
            Object userPrediction = userPredictions.get(i);
            if (matchesAreEqual(matchResult, userPrediction)) {
                correctResult.add(matchResult);
                userPoint++;
            }
        }
        user.setMonthlyScore(updateUserMonthlyScore(user, date, userPoint));
        user.setTotalScore(user.getTotalScore() + userPoint);
        updatePercentGuessedMatches(user);
        userRepository.save(user);

        System.out.println("Результати: " + onlyMatchResult);
        System.out.println("Прогноз користувача " + userName + ": " + userPredictions);
        System.out.println("Правильні прогнози для " + userName + ": " + correctResult);

        return correctResult;
    }

    private long updateUserMonthlyScore(User user, String date, int userPoint) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate inputDate = LocalDate.parse(date, formatter);
        LocalDate currentDate = LocalDate.now();
        if (inputDate.getYear() == currentDate.getYear() && inputDate.getMonth() == currentDate.getMonth()) {
            return user.getMonthlyScore() + userPoint;
        } else {
            return userPoint;
        }
    }

    private void updatePercentGuessedMatches(User user) {
        String competitionName = "allCompetition";
        Optional<Match> matchOptional = matchRepository.findByCompetitionName(competitionName);
        if (matchOptional.isPresent()) {
            Match match = matchOptional.get();
            long totalMatches = match.getTotalMatches();
            if (totalMatches > 0) {
                long percentGuessed = Math.round((double) user.getTotalScore() / totalMatches * 100);
                user.setPercentGuessedMatches(percentGuessed);
            }
        }
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
                return team1ResultScore == team1PredictionScore && team2ResultScore == team2PredictionScore;
            }
        }
        return false;
    }

    private int extractScore(String teamResult) {
        String[] parts = teamResult.split(" ");
        return Integer.parseInt(parts[parts.length - 1]);
    }
}