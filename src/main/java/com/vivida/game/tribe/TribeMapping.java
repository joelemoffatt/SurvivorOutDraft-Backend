package com.vivida.game.tribe;

import com.vivida.game.castaway.CastawayPerformance;
import com.vivida.game.episode.Episode;
import com.vivida.game.season.Season;

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
public class TribeMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "season_id", nullable = false)
    private Season season;

    @ManyToOne
    @JoinColumn(name = "episode_id", nullable = false)
    private Episode episode;

    @ManyToOne
    @JoinColumn(name = "castaway_performance_id", nullable = false)
    private CastawayPerformance castawayPerformance;

    @ManyToOne
    @JoinColumn(name = "tribe_id", nullable = false)
    private Tribe tribe;

    @Column(nullable = false)
    private String status; // e.g., "Swapped", "Merged", "Original"
}
