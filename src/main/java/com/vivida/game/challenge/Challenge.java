package com.vivida.game.challenge;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

import com.vivida.game.episode.Episode;
import com.vivida.game.season.Season;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Challenge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "season_id")
    private Season season;

    @ManyToOne
    @JoinColumn(name = "episode_id", nullable = false)
    private Episode episode;

    @Column(nullable = false)
    private Integer challenge_id;

    @Column(nullable = false)
    private Integer challenge_number;

    @Column(nullable = false)
    private String challenge_type; // "Tribal Immunity", "Individual Reward", etc.

    @Column(nullable = false)
    private String name;

    private Boolean balance;
    private Boolean endurance;
    private Boolean puzzle;
    private Boolean precision;
    private Boolean water;

    @OneToMany(mappedBy = "challenge", cascade = CascadeType.ALL)
    private Set<ChallengePerformance> challengesPerformances;
}
