package ua.dymohlo.FootballPredictions.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.dymohlo.FootballPredictions.Entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUserName(String userName);

    long count();
}