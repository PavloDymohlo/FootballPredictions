package ua.dymohlo.FootballPredictions.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_name")
    private String userName;
    @Column(name = "ranking_position")
    private long rankingPosition;
    @Column(name = "trophy_count")
    private long trophyCount;
    @Column(name = "monthly_score")
    private long monthlyScore;
    @Column(name = "total_score")
    private long totalScore;
    @Column(name = "percent_guessed_matches")
    private long percentGuessedMatches;
    @Column(name = "password")
    private String password;
}