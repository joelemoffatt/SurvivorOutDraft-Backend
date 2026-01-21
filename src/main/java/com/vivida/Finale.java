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
public class Finale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "season_id")
    private Season season;

    private Integer episodeNumber;
    private String episodeTitle;
    private String episodeDate;
    private Integer episodeLength;

    @OneToMany(mappedBy = "finale", cascade = CascadeType.ALL)
    private List<Journey> journeys;

    @OneToMany(mappedBy = "finale", cascade = CascadeType.ALL)
    private List<AdvantageMovement> advantageMovements;

    @OneToMany(mappedBy = "finale", cascade = CascadeType.ALL)
    private List<Challenge> challenges;

    @OneToMany(mappedBy = "finale", cascade = CascadeType.ALL)
    private List<JuryVote> votes;
}
