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
public class JuryVote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "finale_id")
    private Finale finale;

    @ManyToOne
    @JoinColumn(name = "castaway_id")
    private CastawayPerformance castaway;

    @ManyToOne
    @JoinColumn(name = "voted_for_id")
    private CastawayPerformance votedFor;
}
