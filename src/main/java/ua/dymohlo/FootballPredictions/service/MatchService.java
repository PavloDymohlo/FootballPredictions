package ua.dymohlo.FootballPredictions.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachePut;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import ua.dymohlo.FootballPredictions.Entity.Competition;
import ua.dymohlo.FootballPredictions.Entity.User;
import ua.dymohlo.FootballPredictions.component.MatchParser;
import ua.dymohlo.FootballPredictions.repository.CompetitionRepository;
import org.springframework.cache.annotation.Cacheable;
import com.fasterxml.jackson.core.type.TypeReference;
import ua.dymohlo.FootballPredictions.repository.UserRepository;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchService {
    private final CompetitionRepository competitionRepository;
    private final ApplicationContext applicationContext;
    private final MatchParser matchParser;
    private final WebClient webClient;
    private final CacheManager cacheManager;
    @Value("${docs.football-data.url}")
    private String apiUrl;
    @Value("${docs.football-data.key}")
    private String apiKey;

    public List<Object> getFutureMatches() {
        String targetDate = LocalDate.now().plusDays(1).toString();
        Cache cache = cacheManager.getCache("matchesCache");
        if (cache != null) {
            List<Object> cachedMatches = cache.get(targetDate, List.class);
            if (cachedMatches != null && !cachedMatches.isEmpty()) {
                return cachedMatches;
            }
        }
        List<Competition> competitions = competitionRepository.findAll();
        List<String> competitionApiIds = competitions.stream()
                .map(Competition::getCompetitionApiId)
                .collect(Collectors.toList());
        List<Object> combinedMatches = new ArrayList<>();
        for (String competitionApiId : competitionApiIds) {
            List<Object> matches = getMatchService().getMatchesForDate(competitionApiId, targetDate);
            combinedMatches.addAll(matches);
        }
        log.info("List of matches on the date " + targetDate + ": " + combinedMatches);
        if (!combinedMatches.isEmpty() && cache != null) {
            cache.put(targetDate, combinedMatches);
        }
        return combinedMatches;
    }


    public List<Object> getMatchesResultFromApi() {
        String targetDate = LocalDate.now().minusDays(1).toString();
        List<Competition> competitions = competitionRepository.findAll();
        List<String> competitionApiIds = competitions.stream()
                .map(Competition::getCompetitionApiId)
                .collect(Collectors.toList());
        List<Object> combinedMatches = new ArrayList<>();
        for (String competitionApiId : competitionApiIds) {
            List<Object> matches = getMatchService().getMatchesForDate(competitionApiId, targetDate);
            combinedMatches.addAll(matches);
        }
        log.info("List of matches on the date " + targetDate + ": " + combinedMatches);
        Cache cache = cacheManager.getCache("matchesCache");
        if (!combinedMatches.isEmpty() && cache != null) {
            cache.put(targetDate, combinedMatches);
        }
        return combinedMatches;
    }


    private MatchService getMatchService() {
        return applicationContext.getBean(MatchService.class);
    }

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