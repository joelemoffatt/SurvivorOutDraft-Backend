package com.vivida;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

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

    @Column(nullable = true)
    private Boolean balance;

    @Column(nullable = true)
    private Boolean endurance;

    @Column(nullable = true)
    private Boolean puzzle;

    @Column(nullable = true)
    private Boolean precision;

    @Column(nullable = true)
    private Boolean water;

    @OneToMany(mappedBy = "challenge", cascade = CascadeType.ALL)
    private List<ChallengePerformance> challengesPerformances;
}
