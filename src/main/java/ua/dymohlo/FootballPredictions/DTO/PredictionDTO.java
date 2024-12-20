package ua.dymohlo.FootballPredictions.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PredictionDTO implements Serializable {
    private String userName;
    private List<Object> predictions;
    private String matchDate;
}