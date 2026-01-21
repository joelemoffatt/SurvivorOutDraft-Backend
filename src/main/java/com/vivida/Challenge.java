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
    @JoinColumn(name = "episode_id")
    private Episode episode;

    @ManyToOne
    @JoinColumn(name = "finale_id")
    private Finale finale;

    private Integer challengeNumber;
    private String challengeType;
    private String name;
    private String reward;

    @OneToMany(mappedBy = "challenge", cascade = CascadeType.ALL)
    private List<ChallengePerformance> challengesPerformances;
}
