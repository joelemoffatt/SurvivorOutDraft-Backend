package com.vivida.draft;

import com.vivida.auth.User;
import com.vivida.game.castaway.CastawayPerformance;
import com.vivida.social.team.Team;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "draft_picks", uniqueConstraints = {
    @UniqueConstraint(name = "uk_draft_picks_draft_pick_number", columnNames = {"draft_id", "pick_number"}),
    @UniqueConstraint(name = "uk_draft_picks_draft_team_castaway", columnNames = {"draft_id", "team_id", "castaway_performance_id"})
}, indexes = {
    @Index(name = "idx_draft_picks_draft_id", columnList = "draft_id"),
    @Index(name = "idx_draft_picks_team_id", columnList = "team_id"),
    @Index(name = "idx_draft_picks_user_id", columnList = "user_id"),
    @Index(name = "idx_draft_picks_castaway_performance_id", columnList = "castaway_performance_id")
})
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DraftPick {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "draft_id", nullable = false)
    private Draft draft;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    /**
     * Null until the pick is made. Pre-created pick slots have no castaway assigned.
     */
    @ManyToOne
    @JoinColumn(name = "castaway_performance_id")
    private CastawayPerformance castawayPerformance;

    @Column(name = "pick_number", nullable = false)
    private Integer pickNumber;

    @Column(name = "round_number", nullable = false)
    private Integer roundNumber;

    @Column(name = "draft_position", nullable = false)
    private Integer draftPosition;

    /** Null until the pick is made. */
    private LocalDateTime pickedAt;

    @PrePersist
    protected void onCreate() {
        // pickedAt intentionally left null until castaway is selected
    }
}