package com.vivida;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
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
    private Integer id; // Encoded as: seasonNum * 1000 + episodeNum (e.g., 1005 for Season 1 Episode 5)

    @ManyToOne
    @JoinColumn(name = "season_id", nullable = false)
    private Season season;

    @Column(nullable = false)
    private Integer episodeNumber;
    @Column(length = 500, nullable = false)
    private String episodeTitle;
    @Column(nullable = false)
    private String episodeDate;
    @Column(nullable = false)
    private Integer episodeLength;
    @Column(nullable = false)
    private Boolean isFinale;
    @Lob
    @Column(columnDefinition = "TEXT")
    private String episodeSummary;

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
