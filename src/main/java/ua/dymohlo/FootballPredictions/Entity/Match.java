package ua.dymohlo.FootballPredictions.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "matches")
public class Match {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "competition_name")
    private String competitionName;
    @Column(name = "total_matches")
    private long totalMatches;
}
