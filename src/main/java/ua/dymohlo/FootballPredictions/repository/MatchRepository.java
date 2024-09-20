package ua.dymohlo.FootballPredictions.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.dymohlo.FootballPredictions.Entity.Match;
import ua.dymohlo.FootballPredictions.Entity.User;

import java.util.Optional;
public interface MatchRepository extends JpaRepository<Match, Long> {
    Optional<Match> findByCompetitionName(String matchName);
}
