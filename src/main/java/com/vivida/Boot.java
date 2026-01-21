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
public class Boot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "episode_id")
    private Episode episode;

    @OneToOne
    @JoinColumn(name = "tribal_id")
    private Tribal tribal;

    @ManyToOne
    @JoinColumn(name = "castaway_performance_id")
    private CastawayPerformance castaway;
    private Integer order;
    private String event; // e.g., "votedOut", "quit", "medEvac", "lostFire"
}
