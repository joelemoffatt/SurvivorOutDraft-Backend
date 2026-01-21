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
public class TribeEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "episode_id")
    private Episode episode;

    @ManyToOne
    @JoinColumn(name = "castaway_performance_id")
    private CastawayPerformance castaway;

    @ManyToOne
    @JoinColumn(name = "old_tribe_id")
    private Tribe oldTribe;

    @ManyToOne
    @JoinColumn(name = "new_tribe_id")
    private Tribe newTribe;

    private String type; // e.g., "Swap", "Merge", "Original", "Other"
}
