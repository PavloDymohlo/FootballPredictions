package ua.dymohlo.FootballPredictions.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import ua.dymohlo.FootballPredictions.DTO.MatchStatusDto;
import ua.dymohlo.FootballPredictions.DTO.PredictionDTO;
import ua.dymohlo.FootballPredictions.DTO.RegisterDto;
import ua.dymohlo.FootballPredictions.DTO.LoginInDto;
import ua.dymohlo.FootballPredictions.Entity.User;
import ua.dymohlo.FootballPredictions.component.MatchParser;
import ua.dymohlo.FootballPredictions.configuration.PasswordEncoderConfig;
import ua.dymohlo.FootballPredictions.repository.CompetitionRepository;
import ua.dymohlo.FootballPredictions.repository.UserRepository;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class UserService {
//    private final UserRepository userRepository;
//    private final CacheManager cacheManager;
//    private final WebClient webClient;
//    private final MatchParser matchParser;
//    private final ApplicationContext applicationContext;
//    private final RedisTemplate<String, Object> redisTemplate;
//    private final CompetitionRepository competitionRepository;
//    private final MatchService matchService;
//
//    public User register(RegisterDto registerDto) {
//        long startCount = 0;
//        long userRankingPosition = calculateUserRankingPositionDuringRegistration();
//        String userName = registerDto.getUserName();
//        checkUserNameInLatin(userName);
//        String passwordEncoded = PasswordEncoderConfig.encoderPassword(registerDto.getPassword());
//        if (userRepository.findByUserName(userName).isPresent()) {
//            throw new IllegalArgumentException("Цей псевдонім вже використовується!");
//        }
//        User user = User.builder()
//                .userName(userName)
//                .password(passwordEncoded)
//                .rankingPosition(userRankingPosition)
//                .trophyCount(startCount)
//                .monthlyScore(startCount)
//                .totalScore(startCount)
//                .predictionCount(startCount)
//                .percentGuessedMatches((int) startCount)
//                .build();
//        return userRepository.save(user);
//    }
//
//    private void checkUserNameInLatin(String userName) {
//        if (userName.chars().anyMatch(ch -> Character.UnicodeBlock.of(ch).equals(Character.UnicodeBlock.CYRILLIC))) {
//            throw new IllegalArgumentException("Введіть псевдонім латинськими символами!");
//        }
//    }
//
//    private long calculateUserRankingPositionDuringRegistration() {
//        return userRepository.count() + 1;
//    }
//
//    public String loginIn(LoginInDto userLoginInDto) {
//        Optional<User> user = userRepository.findByUserName(userLoginInDto.getUserName());
//        if (user.isEmpty()) {
//            throw new NoSuchElementException("Невірний логін");
//        }
//        if (!PasswordEncoderConfig.checkPassword(userLoginInDto.getPassword(), user.get().getPassword())) {
//            throw new IllegalArgumentException("Невірний пароль");
//        }
//        return "Success";
//    }
//
//    @CachePut(value = "userPredictions", key = "#predictionDTO.userName + '_' + #predictionDTO.matchDate")
//    public PredictionDTO cacheUsersPredictions(PredictionDTO predictionDTO) {
//        log.info("Caching predictions for user: {} on date: {}", predictionDTO.getUserName(), predictionDTO.getMatchDate());
//        Optional<User> optionalUser = userRepository.findByUserName(predictionDTO.getUserName());
//        User user = optionalUser.get();
//        long sumNewPredictions = matchParser.countTotalMatches(predictionDTO.getPredictions());
//        user.setPredictionCount(user.getPredictionCount() + sumNewPredictions);
//        userRepository.save(user);
//        log.info("The number of forecasts for the user " + user.getUserName() + "  increased by " + sumNewPredictions + " forecasts.");
//        return predictionDTO;
//    }
//
//    public void countUsersPredictionsResult() {
//        List<User> users = userRepository.findAll();
//        String date = LocalDate.now().minusDays(1).toString();
//        users.forEach(user -> {
//            updateUserScores(user.getUserName(), date);
//        });
//    }
//
////    public List<Object> comparePredictionsWithResults(String userName, String date) {
////        List<Object> results = matchService.getMatchesFromCacheByDate(date);
////        PredictionDTO predictions = getUsersPredictions(userName, date);
////        List<Object> onlyMatchResult = matchesResultParser(results);
////        List<Object> userPredictions = userPredictionsParser(predictions);
////        List<Object> correctResult = new ArrayList<>();
////        int numberOfMatches = Math.min(onlyMatchResult.size(), userPredictions.size());
////        for (int i = 0; i < numberOfMatches; i++) {
////            Object matchResult = onlyMatchResult.get(i);
////            Object userPrediction = userPredictions.get(i);
////            if (matchesAreEqual(matchResult, userPrediction)) {
////                correctResult.add(matchResult);
////            }
////        }
////        return correctResult;
////    }
//
//    public List<Object> comparePredictionsWithResults(String userName, String date) {
//        List<Object> correctResult = new ArrayList<>();
//        List<Object> results = matchService.getMatchesFromCacheByDate(date);
//        if (results == null) {
//            return correctResult;
//        }
//        PredictionDTO predictions = getUsersPredictions(userName, date);
//        List<Object> onlyMatchResult = matchesResultParser(results);
//        List<Object> userPredictions = userPredictionsParser(predictions);
//
//        int numberOfMatches = Math.min(onlyMatchResult.size(), userPredictions.size());
//        for (int i = 0; i < numberOfMatches; i++) {
//            Object matchResult = onlyMatchResult.get(i);
//            Object userPrediction = userPredictions.get(i);
//            if (matchesAreEqual(matchResult, userPrediction)) {
//                correctResult.add(matchResult);
//            }
//        }
//        return correctResult;
//    }
//
//    public void updateUserScores(String userName, String date) {
//        Optional<User> optionalUser = userRepository.findByUserName(userName);
//        if (!optionalUser.isPresent()) {
//            return;
//        }
//        User user = optionalUser.get();
//        List<Object> correctResults = comparePredictionsWithResults(userName, date);
//        int userPoint = correctResults.size();
//        user.setMonthlyScore(user.getMonthlyScore() + userPoint);
//        user.setTotalScore(user.getTotalScore() + userPoint);
//        user.setPercentGuessedMatches(updatePercentGuessedMatches(user));
//        log.info("update data for user "+ userName);
//        userRepository.save(user);
//    }
//
//    @Cacheable(value = "userPredictions", key = "#userName + '_' + #matchDate", unless = "#result == null")
//    public PredictionDTO getUsersPredictions(String userName, String matchDate) {
//        String key = "userPredictions::" + userName + "_" + matchDate;
//        Object cachedValue = redisTemplate.opsForValue().get(key);
//        if (cachedValue instanceof PredictionDTO) {
//            System.out.println("for front: " + cachedValue);
//            return (PredictionDTO) cachedValue;
//        }
//        return null;
//    }
//
//    private List<Object> matchesResultParser(List<Object> results) {
//        List<Object> onlyMatchResults = new ArrayList<>();
//        for (Object result : results) {
//            if (!(result instanceof Map)) {
//                onlyMatchResults.add(result);
//            }
//        }
//        return onlyMatchResults;
//    }
//
//    private List<Object> userPredictionsParser(PredictionDTO predictions) {
//        List<Object> userPredictions = new ArrayList<>();
//        if (predictions != null) {
//            for (Object prediction : predictions.getPredictions()) {
//                if (!(prediction instanceof Map)) {
//                    userPredictions.add(prediction);
//                }
//            }
//        }
//        return userPredictions;
//    }
//
//    private boolean matchesAreEqual(Object matchResult, Object userPrediction) {
//        if (matchResult instanceof List && userPrediction instanceof List) {
//            List<?> matchList = (List<?>) matchResult;
//            List<?> predictionList = (List<?>) userPrediction;
//            if (matchList.size() == predictionList.size() && matchList.size() == 2) {
//                String team1Result = (String) matchList.get(0);
//                String team1Prediction = (String) predictionList.get(0);
//                String team2Result = (String) matchList.get(1);
//                String team2Prediction = (String) predictionList.get(1);
//                int team1ResultScore = extractScore(team1Result);
//                int team1PredictionScore = extractScore(team1Prediction);
//                int team2ResultScore = extractScore(team2Result);
//                int team2PredictionScore = extractScore(team2Prediction);
//                return team1ResultScore == team1PredictionScore && team2ResultScore == team2PredictionScore;
//            }
//        }
//        return false;
//    }
//
//    private int extractScore(String teamResult) {
//        String[] parts = teamResult.split(" ");
//        return Integer.parseInt(parts[parts.length - 1]);
//    }
//
//    private int updatePercentGuessedMatches(User user) {
//        long userPredictionCount = user.getPredictionCount();
//        return (int) Math.round((double) user.getTotalScore() / userPredictionCount * 100);
//    }
//
//    public List<User> rankingPosition() {
//        List<User> users = userRepository.findAll();
//        Collections.sort(users, (u1, u2) -> {
//            int totalScoreComparison = Long.compare(u2.getTotalScore(), u1.getTotalScore());
//            if (totalScoreComparison != 0) {
//                return totalScoreComparison;
//            }
//            int trophyCountComparison = Long.compare(u2.getTrophyCount(), u1.getTrophyCount());
//            if (trophyCountComparison != 0) {
//                return trophyCountComparison;
//            }
//            return Long.compare(u2.getMonthlyScore(), u1.getMonthlyScore());
//        });
//        long currentRank = 1;
//        long sameRankCount = 0;
//        for (int i = 0; i < users.size(); i++) {
//            User currentUser = users.get(i);
//            if (i > 0) {
//                User previousUser = users.get(i - 1);
//                if (currentUser.getTotalScore() == previousUser.getTotalScore() &&
//                        currentUser.getTrophyCount() == previousUser.getTrophyCount() &&
//                        currentUser.getMonthlyScore() == previousUser.getMonthlyScore()) {
//                    sameRankCount++;
//                } else {
//                    currentRank += sameRankCount + 1;
//                    sameRankCount = 0;
//                }
//            }
//            currentUser.setRankingPosition(currentRank);
//        }
//        log.info("New ranking positions for all users");
//        userRepository.saveAll(users);
//        return users;
//    }
//
//    public List<User> userTrophyCount() {
//        List<User> users = userRepository.findAll();
//        OptionalLong maxMonthlyScoreOpt = users.stream()
//                .mapToLong(User::getMonthlyScore)
//                .max();
//        if (maxMonthlyScoreOpt.isEmpty()) {
//            return users;
//        }
//        long maxMonthlyScore = maxMonthlyScoreOpt.getAsLong();
//        List<User> usersWithMaxScore = users.stream()
//                .filter(user -> user.getMonthlyScore() == maxMonthlyScore)
//                .collect(Collectors.toList());
//        if (usersWithMaxScore.size() == 1) {
//            User topUser = usersWithMaxScore.get(0);
//            topUser.setTrophyCount(topUser.getTrophyCount() + 1);
//            log.info("User "+usersWithMaxScore+" had the trophy");
//            userRepository.save(topUser);
//        }
//        resetUserMonthlyScore(users);
//        return users;
//    }
//
//    private List<User> resetUserMonthlyScore(List<User> users) {
//        long startMonthPoint = 0;
//        List<User> resetUserMonthlyScore = users.stream()
//                .peek(user -> user.setMonthlyScore(startMonthPoint))
//                .collect(Collectors.toList());
//        userRepository.saveAll(resetUserMonthlyScore);
//        return resetUserMonthlyScore;
//    }
//
//
//    public List<String> getFutureMatchesFromCache(String userName, String date) {
//        Cache matchesCache = cacheManager.getCache("matchesCache");
//        if (matchesCache == null) {
//            return Collections.emptyList();
//        }
//        String matchCacheKey = "matchesCache::" + date;
//        Object matchCacheValue = redisTemplate.opsForValue().get(matchCacheKey);
//        if (matchCacheValue == null) {
//            return Collections.emptyList();
//        }
//        String userPredictionKey = "userPredictions::" + userName + "_" + date;
//        Object userPredictionCache = redisTemplate.opsForValue().get(userPredictionKey);
//        if (userPredictionCache == null) {
//            return (List<String>) matchCacheValue;
//        }
//        return Collections.emptyList();
//    }
//
////    public List<Object> getAllMatchesWithPredictionStatus(String userName, String date) {
////        List<Object> allMatches = matchService.getMatchesFromCacheByDate(date);
////        List<Object> correctPredictions = comparePredictionsWithResults(userName, date);
////        List<Object> matchesWithStatus = new ArrayList<>();
////        Map<String, Object> currentCompetition = null;
////
////        for (Object match : allMatches) {
////            if (match instanceof Map && ((Map<?, ?>) match).containsKey("competition")) {
////                currentCompetition = (Map<String, Object>) match;
////                matchesWithStatus.add(currentCompetition);
////                continue;
////            }
////            boolean isCorrect = correctPredictions.contains(match);
////            matchesWithStatus.add(new MatchStatusDto(match, isCorrect));
////        }
////        return matchesWithStatus;
////    }
//
//    public List<Object> getAllMatchesWithPredictionStatus(String userName, String date) {
//        List<Object> allMatches = matchService.getMatchesFromCacheByDate(date);
//        if (allMatches == null) {
//            return null;
//        }
//
//        List<Object> correctPredictions = comparePredictionsWithResults(userName, date);
//        List<Object> matchesWithStatus = new ArrayList<>();
//        Map<String, Object> currentCompetition = null;
//
//        for (Object match : allMatches) {
//            if (match instanceof Map && ((Map<?, ?>) match).containsKey("competition")) {
//                currentCompetition = (Map<String, Object>) match;
//                matchesWithStatus.add(currentCompetition);
//                continue;
//            }
//            boolean isCorrect = correctPredictions.contains(match);
//            matchesWithStatus.add(new MatchStatusDto(match, isCorrect));
//        }
//
//        return matchesWithStatus;
//    }
//}

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final CacheManager cacheManager;
    private final WebClient webClient;
    private final MatchParser matchParser;
    private final ApplicationContext applicationContext;
    private final RedisTemplate<String, Object> redisTemplate;
    private final CompetitionRepository competitionRepository;
    private final MatchService matchService;

    public User register(RegisterDto registerDto) {
        long startCount = 0;
        long userRankingPosition = calculateUserRankingPositionDuringRegistration();
        String userName = registerDto.getUserName();
        checkUserNameInLatin(userName);
        String passwordEncoded = PasswordEncoderConfig.encoderPassword(registerDto.getPassword());
        if (userRepository.findByUserName(userName).isPresent()) {
            log.error("user with userName " + userName + " already exists!");
            throw new IllegalArgumentException("Цей псевдонім вже використовується!");
        }
        User user = User.builder()
                .userName(userName)
                .password(passwordEncoded)
                .rankingPosition(userRankingPosition)
                .trophyCount(startCount)
                .monthlyScore(startCount)
                .totalScore(startCount)
                .predictionCount(startCount)
                .percentGuessedMatches((int) startCount)
                .build();
        return userRepository.save(user);
    }

    private void checkUserNameInLatin(String userName) {
        if (userName.chars().anyMatch(ch -> Character.UnicodeBlock.of(ch).equals(Character.UnicodeBlock.CYRILLIC))) {
            throw new IllegalArgumentException("Введіть псевдонім латинськими символами!");
        }
    }

    private long calculateUserRankingPositionDuringRegistration() {
        return userRepository.count() + 1;
    }

    public String loginIn(LoginInDto userLoginInDto) {
        Optional<User> user = userRepository.findByUserName(userLoginInDto.getUserName());
        if (user.isEmpty()) {
            throw new NoSuchElementException("Невірний логін");
        }
        if (!PasswordEncoderConfig.checkPassword(userLoginInDto.getPassword(), user.get().getPassword())) {
            throw new IllegalArgumentException("Невірний пароль");
        }
        return "Success";
    }

    @CachePut(value = "userPredictions", key = "#predictionDTO.userName + '_' + #predictionDTO.matchDate")
    public PredictionDTO cacheUsersPredictions(PredictionDTO predictionDTO) {
        log.info("Caching predictions for user: {} on date: {}", predictionDTO.getUserName(), predictionDTO.getMatchDate());
        Optional<User> optionalUser = userRepository.findByUserName(predictionDTO.getUserName());
        User user = optionalUser.get();
        long sumNewPredictions = matchParser.countTotalMatches(predictionDTO.getPredictions());
        user.setPredictionCount(user.getPredictionCount() + sumNewPredictions);
        userRepository.save(user);
        log.info("The number of forecasts for the user " + user.getUserName() + "  increased by " + sumNewPredictions + " forecasts.");
        return predictionDTO;
    }


    public void countUsersPredictionsResult() {
        List<User> users = userRepository.findAll();
        String date = LocalDate.now().minusDays(1).toString();
        users.forEach(user -> {
            updateUserScores(user.getUserName(), date);
        });
    }

//    public List<Object> comparePredictionsWithResults(String userName, String date) {
//        List<Object> results = matchService.getMatchesFromCacheByDate(date);
//        PredictionDTO predictions = getUsersPredictions(userName, date);
//        List<Object> onlyMatchResult = matchesResultParser(results);
//        List<Object> userPredictions = userPredictionsParser(predictions);
//        List<Object> correctResult = new ArrayList<>();
//        int numberOfMatches = Math.min(onlyMatchResult.size(), userPredictions.size());
//        for (int i = 0; i < numberOfMatches; i++) {
//            Object matchResult = onlyMatchResult.get(i);
//            Object userPrediction = userPredictions.get(i);
//            if (matchesAreEqual(matchResult, userPrediction)) {
//                correctResult.add(matchResult);
//            }
//        }
//        return correctResult;
//    }

    public List<Object> comparePredictionsWithResults(String userName, String date) {
        try{
            List<Object> results = matchService.getMatchesFromCacheByDate(date);
            PredictionDTO predictions = getUsersPredictions(userName, date);
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
        } catch (NullPointerException e){
            String errorMessage = " матчі не відбувалися. Очікуйте наступних результатів.";
            log.error(errorMessage);
            throw new NullPointerException(date+errorMessage);
        }

    }

    public void updateUserScores(String userName, String date) {
        Optional<User> optionalUser = userRepository.findByUserName(userName);
        if (!optionalUser.isPresent()) {
            return;
        }
        User user = optionalUser.get();
        List<Object> correctResults = comparePredictionsWithResults(userName, date);
        int userPoint = correctResults.size();
        user.setMonthlyScore(user.getMonthlyScore() + userPoint);
        user.setTotalScore(user.getTotalScore() + userPoint);
        user.setPercentGuessedMatches(updatePercentGuessedMatches(user));
        userRepository.save(user);
    }

    @Cacheable(value = "userPredictions", key = "#userName + '_' + #matchDate", unless = "#result == null")
    public PredictionDTO getUsersPredictions(String userName, String matchDate) {
        String key = "userPredictions::" + userName + "_" + matchDate;
        Object cachedValue = redisTemplate.opsForValue().get(key);
        if (cachedValue instanceof PredictionDTO) {
            System.out.println("for front: " + cachedValue);
            return (PredictionDTO) cachedValue;
        }
        return null;
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

    private int updatePercentGuessedMatches(User user) {
        long userPredictionCount = user.getPredictionCount();
        return (int) Math.round((double) user.getTotalScore() / userPredictionCount * 100);
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
        userRepository.saveAll(users);
        System.out.println(users);
        return users;
    }

    public List<User> userTrophyCount() {
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

    private List<User> resetUserMonthlyScore(List<User> users) {
        long startMonthPoint = 0;
        List<User> resetUserMonthlyScore = users.stream()
                .peek(user -> user.setMonthlyScore(startMonthPoint))
                .collect(Collectors.toList());
        userRepository.saveAll(resetUserMonthlyScore);
        return resetUserMonthlyScore;
    }


    public List<String> getFutureMatchesFromCache(String userName, String date) {
        Cache matchesCache = cacheManager.getCache("matchesCache");
        if (matchesCache == null) {
            return Collections.emptyList();
        }
        String matchCacheKey = "matchesCache::" + date;
        Object matchCacheValue = redisTemplate.opsForValue().get(matchCacheKey);
        if (matchCacheValue == null) {
            return Collections.emptyList();
        }
        String userPredictionKey = "userPredictions::" + userName + "_" + date;
        Object userPredictionCache = redisTemplate.opsForValue().get(userPredictionKey);
        if (userPredictionCache == null) {
            return (List<String>) matchCacheValue;
        }
        return Collections.emptyList();
    }

    public List<Object> getAllMatchesWithPredictionStatus(String userName, String date) {
        List<Object> allMatches = matchService.getMatchesFromCacheByDate(date);
        List<Object> correctPredictions = comparePredictionsWithResults(userName, date);
        List<Object> matchesWithStatus = new ArrayList<>();
        Map<String, Object> currentCompetition = null;

        for (Object match : allMatches) {
            if (match instanceof Map && ((Map<?, ?>) match).containsKey("competition")) {
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

    public List<User> allUsers() {
        return userRepository.findAll();
    }
}


