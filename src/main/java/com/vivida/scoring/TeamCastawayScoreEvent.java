package com.vivida.scoring;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

import com.vivida.game.castaway.CastawayPerformance;
import com.vivida.social.group.Group;
import com.vivida.social.team.Team;
import com.vivida.social.team.TeamCastaway;

@Entity
@Table(name = "team_castaway_score_events")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TeamCastawayScoreEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToOne
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne
    @JoinColumn(name = "team_castaway_id", nullable = false)
    private TeamCastaway teamCastaway;

    @ManyToOne
    @JoinColumn(name = "castaway_performance_id", nullable = false)
    private CastawayPerformance castawayPerformance;

    @ManyToOne
    @JoinColumn(name = "point_rule_id", nullable = false)
    private PointRule pointRule;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RuleType ruleType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScoreEventSourceType sourceType;

    /**
     * The ID of the source record (e.g., ChallengePerformance.id, AdvantageMovement.id)
     */
    @Column(nullable = false)
    private Integer sourceId;

    /**
     * Episode number when this event occurred
     */
    private Integer episodeNumber;

    /**
     * Display label for the event (e.g., "Found idol", "Made merge", "Won individual immunity")
     */
    @Column(length = 255)
    private String eventLabel;

    /**
     * How many times this occurrence was counted
     * Usually 1, but future-safe if a rule aggregates multiple occurrences
     */
    @Column(nullable = false)
    private Integer countValue = 1;

    /**
     * Points per occurrence from the rule
     */
    @Column(nullable = false)
    private Integer pointsEach;

    /**
     * Multiplier applied to this event (default 1)
     */
    @Column(nullable = false)
    private Integer multiplier = 1;

    /**
     * Total points = countValue * pointsEach * multiplier
     */
    @Column(nullable = false)
    private Integer totalPoints;

    @ManyToOne
    @JoinColumn(name = "calculation_run_id", nullable = false)
    private GroupScoreCalculationRun calculationRun;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (countValue == null) {
            countValue = 1;
        }
        if (multiplier == null) {
            multiplier = 1;
        }
        // totalPoints = countValue * pointsEach * multiplier
        if (totalPoints == null && countValue != null && pointsEach != null && multiplier != null) {
            totalPoints = countValue * pointsEach * multiplier;
        }
    }
}
