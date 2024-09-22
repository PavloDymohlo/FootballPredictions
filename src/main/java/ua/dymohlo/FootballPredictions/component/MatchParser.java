package ua.dymohlo.FootballPredictions.component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class MatchParser {

    private final ObjectMapper objectMapper;

    public List<Object> parseMatches(String json) throws IOException {
        List<Object> matchResults = new ArrayList<>();
        JsonNode rootNode = objectMapper.readTree(json);
        String competitionName = rootNode.path("competition").path("name").asText();
        Map<String, String> competitionInfo = new HashMap<>();
        competitionInfo.put("competition", competitionName);
        matchResults.add(competitionInfo);
        JsonNode matchesNode = rootNode.path("matches");
        for (JsonNode matchNode : matchesNode) {
            String homeTeam = matchNode.path("homeTeam").path("name").asText();
            String awayTeam = matchNode.path("awayTeam").path("name").asText();
            String status = matchNode.path("status").asText();
            String homeScore;
            String awayScore;
            if ("FINISHED".equals(status)) {
                homeScore = matchNode.path("score").path("fullTime").path("home").asText();
                awayScore = matchNode.path("score").path("fullTime").path("away").asText();
            } else {
                homeScore = "?";
                awayScore = "?";
            }
            List<String> matchInfo = new ArrayList<>();
            matchInfo.add(homeTeam + " " + homeScore);
            matchInfo.add(awayTeam + " " + awayScore);
            matchResults.add(matchInfo);
        }
        return matchResults;
    }
    public int countTotalMatches(String json) throws IOException {
        List<Object> parsedMatches = parseMatches(json);
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