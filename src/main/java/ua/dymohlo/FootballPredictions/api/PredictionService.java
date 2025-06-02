package ua.dymohlo.FootballPredictions.api;

import ua.dymohlo.FootballPredictions.DTO.PredictionDTO;

import java.util.List;

public interface PredictionService {
    PredictionDTO saveUserPredictions(PredictionDTO predictionDTO);

    PredictionDTO getUserPredictions(String userName, String matchDate);

    List<Object> getCorrectPredictions(String userName, String date);

    List<Object> getAllMatchesWithPredictionStatus(String userName, String date);

    void processUserPredictionResults(String userName, String date);

    void countAllUsersPredictionsResult();
}