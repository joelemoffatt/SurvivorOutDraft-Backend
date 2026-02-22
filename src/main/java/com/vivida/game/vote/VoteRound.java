package com.vivida.game.vote;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

import com.vivida.game.tribal.Tribal;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class VoteRound {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "tribal_id", nullable = false)
    private Tribal tribal;

    @OneToMany(mappedBy = "voteRound", cascade = CascadeType.ALL)
    private List<Vote> votes;
    
    @Column(nullable = false)
    private Boolean isTie;
    
    private Integer voteOrder;}