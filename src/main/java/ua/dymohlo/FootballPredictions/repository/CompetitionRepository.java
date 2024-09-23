package ua.dymohlo.FootballPredictions.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.dymohlo.FootballPredictions.Entity.Competition;

import java.util.List;
import java.util.Optional;

public interface CompetitionRepository extends JpaRepository<Competition, Long> {
    List<Competition> findAll();
}
