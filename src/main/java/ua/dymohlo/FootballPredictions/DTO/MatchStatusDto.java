package ua.dymohlo.FootballPredictions.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MatchStatusDto {
    private Object match;
    private boolean isPredictedCorrectly;
}
