package ua.dymohlo.FootballPredictions.component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MatchParser {

    private final ObjectMapper objectMapper;

    public List<String> parseMatches(String json) throws IOException {
        List<String> matchResults = new ArrayList<>();
        JsonNode rootNode = objectMapper.readTree(json);
        String competitionName = rootNode.path("competition").path("name").asText();
        matchResults.add(competitionName);
        matchResults.add("");
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
            String homeResult = homeTeam + " " + homeScore;
            String awayResult = awayTeam + " " + awayScore;
            matchResults.add(homeResult);
            matchResults.add(awayResult);
            matchResults.add("");
        }
        return matchResults;
    }

    public int matchesCount(String json) throws IOException {
        List<String> matchResults = parseMatches(json);
        int matchCount = 0;

        // Ітеруємося по результатах та рахуємо рядки, що містять команди
        for (String result : matchResults) {
            if (result.contains(" ?")) { // Перевіряємо наявність команд
                matchCount++;
            }
        }

        // Повертаємо кількість матчів
        return matchCount / 2; // Кожен матч представлений двічі (домашня та виїзна команда)
    }

}
