package com.vivida.game.tribal;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

import com.vivida.game.boot.Boot;
import com.vivida.game.episode.Episode;
import com.vivida.game.tribe.Tribe;
import com.vivida.game.vote.VoteRound;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Tribal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "episode_id", nullable = false)
    private Episode episode;

    @ManyToOne
    @JoinColumn(name = "tribe_id", nullable = false)
    private Tribe tribe;
    
    @Column(nullable = false)
    private Integer bootOrder; // Links to the boot that happened at this tribal

    @OneToMany(mappedBy = "tribal", cascade = CascadeType.ALL)
    private List<VoteRound> votes;

    @OneToOne(mappedBy = "tribal", cascade = CascadeType.ALL)
    private Boot boot;
}
