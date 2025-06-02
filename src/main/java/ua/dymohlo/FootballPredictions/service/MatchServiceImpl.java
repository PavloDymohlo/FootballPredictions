package ua.dymohlo.FootballPredictions.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import ua.dymohlo.FootballPredictions.Entity.User;
import ua.dymohlo.FootballPredictions.api.MatchService;
import ua.dymohlo.FootballPredictions.component.MatchParser;
import ua.dymohlo.FootballPredictions.repository.UserRepository;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchServiceImpl implements MatchService {
    private final ApplicationContext applicationContext;
    private final MatchParser matchParser;
    private final CacheManager cacheManager;
    private final NodeScriptService nodeScriptService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final UserRepository userRepository;

    @Override
    public List<Object> getFutureMatches() {
        String nodeScriptData = nodeScriptService.runNodeScript("future");
        List<Object> parsedMatches = matchParser.parseMatches(nodeScriptData);
        String targetDate = null;
        if (!parsedMatches.isEmpty() && parsedMatches.get(0) instanceof Map) {
            Map<String, Object> firstMatch = (Map<String, Object>) parsedMatches.get(0);
            targetDate = (String) firstMatch.get("date");
        }
        if (targetDate == null) {
            log.warn("No date found in the parsed matches.");
            return new ArrayList<>();
        }
        String dateWithoutDay = targetDate.split(" ")[0];
        Cache cache = cacheManager.getCache("matchesCache");
        if (cache != null) {
            List<Object> cachedMatches = cache.get(dateWithoutDay, List.class);
            if (cachedMatches != null && !cachedMatches.isEmpty()) {
                log.info("Returning cached matches for date: " + dateWithoutDay);
                return cachedMatches;
            } else {
                log.info("Updating cache for date: " + dateWithoutDay);
                cache.put(dateWithoutDay, parsedMatches);
            }
        }
        return parsedMatches;
    }

    @Override
    public List<Object> getMatchesResultFromApi() {
        String nodeScriptData = nodeScriptService.runNodeScript("past");
        List<Object> parsedMatches = matchParser.parseMatches(nodeScriptData);
        String targetDate = null;
        if (!parsedMatches.isEmpty() && parsedMatches.get(0) instanceof Map) {
            Map<String, Object> firstMatch = (Map<String, Object>) parsedMatches.get(0);
            targetDate = (String) firstMatch.get("date");
        }
        if (targetDate == null) {
            log.warn("No date found in the parsed matches.");
            return new ArrayList<>();
        }
        String dateWithoutDay = targetDate.split(" ")[0];
        Cache cache = cacheManager.getCache("matchesCache");
        if (cache != null) {
            List<Object> cachedMatches = cache.get(dateWithoutDay, List.class);
            if (cachedMatches != null && !cachedMatches.isEmpty()) {
                log.info("Updating cache for date: " + dateWithoutDay);
                cache.put(dateWithoutDay, parsedMatches);
            } else {
                log.info("Adding new cache entry for date: " + dateWithoutDay);
                cache.put(dateWithoutDay, parsedMatches);
            }
        }
        int unknownMatchCount = 0;
        for (Object match : parsedMatches) {
            if (match instanceof List) {
                List<String> matchDetails = (List<String>) match;
                for (int i = 0; i < matchDetails.size(); i++) {
                    if (matchDetails.get(i).contains("?")) {
                        unknownMatchCount++;
                        matchDetails.set(i, matchDetails.get(i).replace("?", "н/в"));
                    }
                }
            }
        }
        if (unknownMatchCount > 0) {
            int matchWithoutResult = unknownMatchCount / 2;
            log.info("Number of matches with unknown results: " + matchWithoutResult);
            updateUsersPredictionCount(matchWithoutResult, targetDate);
        }
        return parsedMatches;
    }

    @Override
    public List<Object> getMatchesFromCacheByDate(String targetDate) {
        Cache cache = cacheManager.getCache("matchesCache");
        if (cache != null) {
            List<Object> matches = (List<Object>) cache.get(targetDate).get();
            if (matches != null) {
                return matches;
            }
        }
        return Collections.emptyList();
    }

    private void updateUsersPredictionCount(int matchWithoutResult, String targetDate) {
        String formattedDate = targetDate.split(" ")[0];
        Set<String> cachedKeys = redisTemplate.keys("userPredictions::*_" + formattedDate);
        List<String> usersWithPredictions = new ArrayList<>();
        for (String key : cachedKeys) {
            String[] parts = key.split("::|_");
            if (parts.length >= 2) {
                String userName = parts[1];
                usersWithPredictions.add(userName);
            }
        }
        usersWithPredictions.forEach(userName -> {
            Optional<User> optionalUser = userRepository.findByUserName(userName);
            optionalUser.ifPresent(user -> {
                user.setPredictionCount(user.getPredictionCount() - matchWithoutResult);
                log.info("List of users: " + usersWithPredictions);
                userRepository.save(user);
                log.info("Updated prediction count for user: {}", userName);
            });
        });
    }

    @Override
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
}
