package ua.dymohlo.FootballPredictions.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PredictionDTO {
    private String userName;
    private Map<String, Integer> predictions;
    private LocalDate matchDate;
}
