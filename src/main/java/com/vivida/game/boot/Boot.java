package com.vivida.game.boot;

import com.vivida.game.castaway.CastawayPerformance;
import com.vivida.game.episode.Episode;
import com.vivida.game.tribal.Tribal;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Boot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "episode_id", nullable = false)
    private Episode episode;

    @OneToOne
    @JoinColumn(name = "tribal_id", unique = true)
    private Tribal tribal;

    @ManyToOne
    @JoinColumn(name = "castaway_performance_id", nullable = false)
    private CastawayPerformance castaway;
    @Column(nullable = false)
    private Integer bootOrder;
    @Column(nullable = false)
    private String event; // e.g., "votedOut", "quit", "medEvac", "lostFire", "lostFinalFire"
}
