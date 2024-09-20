package ua.dymohlo.FootballPredictions.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import ua.dymohlo.FootballPredictions.DTO.PredictionDTO;
import ua.dymohlo.FootballPredictions.Entity.Match;
import ua.dymohlo.FootballPredictions.component.MatchParser;
import ua.dymohlo.FootballPredictions.repository.MatchRepository;


import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

//@Service
//@Slf4j
//public class MatchService {
//    private final WebClient webClient;
//    private final MatchParser matchParser;
//    private final String apiKey;
//
//    public MatchService(WebClient.Builder webClientBuilder,
//                        @Value("${docs.football-data.url}") String apiUrl,
//                        @Value("${docs.football-data.key}") String apiKey,
//                        MatchParser matchParser) {
//        this.webClient = webClientBuilder.baseUrl(apiUrl).build();
//        this.matchParser = matchParser;
//        this.apiKey = apiKey;
//    }
//
//    @Cacheable(value = "matchesCache", key = "#leagueId + '_' + #targetDate")
//    public Mono<List<String>> getMatchesForDate(String leagueId, String targetDate) {
//        String uri = String.format("http://api.football-data.org/v4/competitions/%s/matches?dateFrom=%s&dateTo=%s",
//                leagueId, targetDate, targetDate);
//
//        return webClient.get()
//                .uri(uri)
//                .header("X-Auth-Token", apiKey)
//                .retrieve()
//                .bodyToMono(String.class)
//                .map(response -> {
//                    try {
//                        return matchParser.parseMatches(response);
//                    } catch (IOException e) {
//                        throw new RuntimeException(e);
//                    }
//                });
//    }
//
//    @CachePut(value = "userPredictions", key = "#predictionDTO.userName")
//    public Map<String, String> cacheUsersPredictions(PredictionDTO predictionDTO) {
//        String userName = predictionDTO.getUserName();
//        Map<String, Integer> predictions = predictionDTO.getPredictions();
//
//        log.info("Caching predictions for user: {}", userName);
//
//        // Convert predictions to a format suitable for caching
//        Map<String, String> formattedPredictions = predictions.entrySet().stream()
//                .filter(entry -> entry.getKey().contains(" ?"))
//                .collect(Collectors.toMap(
//                        entry -> entry.getKey().split(" \\?")[0].trim(),
//                        entry -> entry.getValue().toString()
//                ));
//
//        log.info("Predictions for user {} have been cached", userName);
//        return formattedPredictions;
//    }
//}


@Service
@RequiredArgsConstructor
@Slf4j
public class MatchService {
    private final WebClient webClient;
    private final MatchParser matchParser;
    private final MatchRepository matchRepository;
    private final ApplicationContext applicationContext;

    @Value("${docs.football-data.url}")
    private String apiUrl;
    @Value("${docs.football-data.key}")
    private String apiKey;


    public List<String> getFutureMatches() {
        String leagueId = "PL";
        String targetDate = "2024-09-21";
        return getMatchService().getMatchesForDate(leagueId, targetDate);
    }
    public List<String> matchesResult(){
        String leagueId = "PL";
        String targetDate = "2024-09-21";
        return getMatchService().getMatchesForDate(leagueId, targetDate);
    }
    private MatchService getMatchService() {
        return applicationContext.getBean(MatchService.class);
    }

//    @Cacheable(value = "matchesCache", key = "#leagueId + '_' + #targetDate")
//    public List<String> getMatchesForDate(String leagueId, String targetDate) {
//        String uri = String.format("http://api.football-data.org/v4/competitions/%s/matches?dateFrom=%s&dateTo=%s",
//                leagueId, targetDate, targetDate);
//        String response = webClient.get()
//                .uri(uri)
//                .header("X-Auth-Token", apiKey)
//                .retrieve()
//                .bodyToMono(String.class)
//                .block();
//        try {
//            int matchCount = matchParser.matchesCount(response);
//            String matchName = "allCompetition";
//            Optional<Match> match = matchRepository.findByCompetitionName(matchName);
//            match.ifPresent(m -> {
//                m.setTotalMatches(m.getTotalMatches() + matchCount);
//                matchRepository.save(m);
//            });
//            return matchParser.parseMatches(response);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
@Cacheable(value = "matchesCache", key = "#leagueId + '_' + #targetDate")
public List<String> getMatchesForDate(String leagueId, String targetDate) {
    String uri = String.format("http://api.football-data.org/v4/competitions/%s/matches?dateFrom=%s&dateTo=%s",
            leagueId, targetDate, targetDate);
    String response = webClient.get()
            .uri(uri)
            .header("X-Auth-Token", apiKey)
            .retrieve()
            .bodyToMono(String.class)
            .block();
    try {
        int matchCount = matchParser.matchesCount(response);
        String matchName = "allCompetition";
        Optional<Match> match = matchRepository.findByCompetitionName(matchName);
        match.ifPresent(m -> {
            m.setTotalMatches(m.getTotalMatches() + matchCount);
            matchRepository.save(m);
        });
        return matchParser.parseMatches(response);
    } catch (IOException e) {
        throw new RuntimeException(e);
    }
}

@CachePut(value = "userPredictions", key = "#predictionDTO.userName + '_' + #predictionDTO.matchDate")
public List<String> cacheUsersPredictions(PredictionDTO predictionDTO) {
    String userName = predictionDTO.getUserName();
    Map<String, Integer> predictions = predictionDTO.getPredictions();
    log.info("Caching predictions for user: {}", userName);
    List<String> formattedPredictions = predictions.keySet().stream()
            .map(match -> match + " " + predictions.get(match))
            .collect(Collectors.toList());
    log.info("Predictions for user {} have been cached: {}", userName, formattedPredictions);
    return formattedPredictions;
}


//    public List<String> compareUsersPredictions(){
//
//    }

}