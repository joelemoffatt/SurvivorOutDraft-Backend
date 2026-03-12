package com.vivida.scoring;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

import com.vivida.game.season.Season;
import com.vivida.social.group.Group;

@Entity
@Table(name = "group_score_calculation_runs")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GroupScoreCalculationRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToOne
    @JoinColumn(name = "season_id", nullable = false)
    private Season season;

    /**
     * Latest episode number that was included in this calculation
     */
    private Integer latestEpisodeNumber;

    /**
     * First episode number that was included in this calculation
     */
    private Integer firstScoringEpisodeNumber;

    /**
     * The rule version at the time this calculation was run
     */
    @Column(nullable = false)
    private Integer ruleVersion;

    /**
     * The game data version at the time this calculation was run
     */
    @Column(nullable = false)
    private Integer gameDataVersion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CalculationRunStatus status;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    /**
     * Error message if status is FAILED
     */
    @Column(length = 1000)
    private String errorMessage;

    @OneToMany(mappedBy = "calculationRun", cascade = CascadeType.ALL)
    private List<TeamCastawayScoreEvent> scoreEvents;

    @PrePersist
    protected void onCreate() {
        startedAt = LocalDateTime.now();
        if (status == null) {
            status = CalculationRunStatus.RUNNING;
        }
    }
}
