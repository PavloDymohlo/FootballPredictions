package ua.dymohlo.FootballPredictions.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "competitions")
public class Competition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "competition_name")
    private String competitionName;
    @Column(name = "competition_api_id")
    private String competitionApiId;
}