package com.vivida.game.vote;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.vivida.game.castaway.CastawayPerformance;

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

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "vote_round_id", nullable = false)
    private VoteRound voteRound;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "castaway_id", nullable = false)
    private CastawayPerformance castaway;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "voted_for_id", nullable = false)
    private CastawayPerformance votedFor;

    private Boolean nullified;

    @JsonProperty("voteRoundId")
    public Integer getVoteRoundId() {
        return voteRound != null ? voteRound.getId() : null;
    }

    @JsonProperty("castawayId")
    public Integer getCastawayId() {
        return castaway != null ? castaway.getId() : null;
    }

    @JsonProperty("votedForId")
    public Integer getVotedForId() {
        return votedFor != null ? votedFor.getId() : null;
    }
}
