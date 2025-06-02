package ua.dymohlo.FootballPredictions.api;

import java.util.List;

public interface MatchService {
    List<Object> getFutureMatches();
    List<Object> getMatchesResultFromApi();
    List<Object> getMatchesFromCacheByDate(String targetDate);
    List<String> getFutureMatchesFromCache(String userName, String date);
}