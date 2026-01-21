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
public class Episode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "season_id")
    private Season season;

    private Integer episodeNumber;
    private String episodeTitle;
    private String episodeDate;
    private Integer episodeLength;

    @OneToMany(mappedBy = "episode", cascade = CascadeType.ALL)
    private List<Journey> journeys;

    @OneToMany(mappedBy = "episode", cascade = CascadeType.ALL)
    private List<AdvantageMovement> advantageMovements;

    @OneToMany(mappedBy = "episode", cascade = CascadeType.ALL)
    private List<Challenge> challenges;

    @OneToMany(mappedBy = "episode", cascade = CascadeType.ALL)
    private List<Tribal> tribals;

    @OneToMany(mappedBy = "episode", cascade = CascadeType.ALL)
    private List<Boot> nonVotedBoots; // Boots that were not due to tribal council votes
}
