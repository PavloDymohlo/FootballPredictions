package ua.dymohlo.FootballPredictions.component;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

@Component
@RequiredArgsConstructor
public class MatchParser {

    private final ObjectMapper objectMapper;

//    public List<Object> parseMatches(String json) throws IOException {
//        List<Object> matchResults = new ArrayList<>();
//        JsonNode rootNode = objectMapper.readTree(json);
//        String competitionName = rootNode.path("competition").path("name").asText();
//        Map<String, String> competitionInfo = new HashMap<>();
//        competitionInfo.put("competition", competitionName);
//        matchResults.add(competitionInfo);
//        JsonNode matchesNode = rootNode.path("matches");
//        for (JsonNode matchNode : matchesNode) {
//            String homeTeam = matchNode.path("homeTeam").path("name").asText();
//            String awayTeam = matchNode.path("awayTeam").path("name").asText();
//            String status = matchNode.path("status").asText();
//            String homeScore;
//            String awayScore;
//            if ("FINISHED".equals(status)) {
//                homeScore = matchNode.path("score").path("fullTime").path("home").asText();
//                awayScore = matchNode.path("score").path("fullTime").path("away").asText();
//            } else {
//                homeScore = "?";
//                awayScore = "?";
//            }
//            List<String> matchInfo = new ArrayList<>();
//            matchInfo.add(homeTeam + " " + homeScore);
//            matchInfo.add(awayTeam + " " + awayScore);
//            matchResults.add(matchInfo);
//        }
//        return matchResults;
//    }

    public List<Object> parseMatches(String json) {
        ObjectMapper objectMapper = new ObjectMapper();
        List<Object> result = new ArrayList<>();
        try {
            List<Map<String, Object>> matches = objectMapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {});
            for (Map<String, Object> match : matches) {
                if (match.containsKey("date") && result.isEmpty()) {
                    result.add(Map.of("date", match.get("date")));
                }
                if (match.containsKey("country") && match.containsKey("tournament")) {
                    result.add(Map.of("country", match.get("country"), "tournament", match.get("tournament")));
                }
                if (match.containsKey("matches")) {
                    List<List<String>> matchDetails = (List<List<String>>) match.get("matches");
                    for (List<String> game : matchDetails) {
                        result.add(game);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public long countTotalMatches(List<Object> parsedMatches) {
        int totalMatches = 0;
        for (Object obj : parsedMatches) {
            if (obj instanceof List) {
                List<?> matchInfo = (List<?>) obj;
                if (matchInfo.size() == 2) {
                    totalMatches++;
                }
            }
        }
        return totalMatches;
    }
}