package com.vivida;

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
public class CastawayPerformance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "season_id")
    private Season season;

    @ManyToOne
    @JoinColumn(name = "castaway_id")
    private Castaway castaway;

    @ManyToOne
    @JoinColumn(name = "original_tribe_id")
    private Tribe originalTribe;

    private String order;
    private String place;

    private Boolean jury;
    private Boolean finalist;
    private Boolean winner;
    private Boolean voted_out;
    private Boolean medically_evacuated;
    private Boolean quit;
}
