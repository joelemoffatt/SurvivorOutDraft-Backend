package com.vivida.social.team;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

import com.vivida.game.castaway.CastawayPerformance;

@Entity
@Table(name = "team_castaways", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"team_id", "castaway_performance_id"})
})
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TeamCastaway {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne
    @JoinColumn(name = "castaway_performance_id", nullable = false)
    private CastawayPerformance castawayPerformance;

    private Integer draftOrder;

    @Column(nullable = false)
    private Integer points = 0;

    @Column(nullable = false)
    private LocalDateTime draftedAt;

    @PrePersist
    protected void onCreate() {
        draftedAt = LocalDateTime.now();
        if (points == null) {
            points = 0;
        }
    }
}
