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
public class Vote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "vote_round_id")
    private VoteRound voteRound;

    @ManyToOne
    @JoinColumn(name = "castaway_id")
    private CastawayPerformance castaway;

    @ManyToOne
    @JoinColumn(name = "voted_for_id")
    private CastawayPerformance votedFor;
    private Boolean nullified;
}
