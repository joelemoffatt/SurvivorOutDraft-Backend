package com.vivida.game.season;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.vivida.game.episode.Episode;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class Season {

    @Id
    @JsonProperty("seasonId")
    private Integer season;

    @Column(nullable = false)
    private String version;

    @Column(length = 500)
    private String seasonName;

    @Column(length = 500)
    private String location;

    @Column(length = 255)
    private String country;

    @Column(length = 1000)
    private String tribeSetup;

    @Column(length = 500)
    private String fullName;

    @Column(length = 50)
    private String filmingStarted;

    @Column(length = 50)
    private String filmingEnded;

    @Column(length = 50)
    private String premiereDate;

    @Column(length = 50)
    private String endingDate;

    @Column
    private Integer viewers;

    @Column
    private Integer episodesNumber;

    @JsonIgnore
    @OneToMany(mappedBy = "season", cascade = CascadeType.ALL)
    private List<Episode> episodes;
}
