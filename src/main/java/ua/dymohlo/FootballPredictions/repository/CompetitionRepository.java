package ua.dymohlo.FootballPredictions.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.dymohlo.FootballPredictions.Entity.Competition;

import java.util.List;

public interface CompetitionRepository extends JpaRepository<Competition, Long> {
    List<Competition> findAll();
}