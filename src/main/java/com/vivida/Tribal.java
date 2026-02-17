package com.vivida;

import jakarta.persistence.*;
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
