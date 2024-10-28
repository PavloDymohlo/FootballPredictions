package ua.dymohlo.FootballPredictions.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import ua.dymohlo.FootballPredictions.component.MatchParser;

import java.util.*;


@Service
@RequiredArgsConstructor
@Slf4j
public class MatchService {
    private final ApplicationContext applicationContext;
    private final MatchParser matchParser;
    private final CacheManager cacheManager;
    private final NodeScriptService nodeScriptService;

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
        return parsedMatches;
    }

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
}