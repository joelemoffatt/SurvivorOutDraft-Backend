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
public class AdvantageMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "castaway_id", nullable = false)
    private CastawayPerformance castawayId;

    @ManyToOne
    @JoinColumn(name = "played_for_id")
    private CastawayPerformance playedForId;

    @ManyToOne
    @JoinColumn(name = "episode_id", nullable = false)
    private Episode episode;

    @Column(nullable = false)
    private String event;
    @Column(nullable = false)
    private String advantageType;
    private String success;
    private Integer votesNullified;
}
