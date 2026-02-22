package com.vivida.game.tribe;

import com.vivida.game.season.Season;

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
public class Tribe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "season_id", nullable = false)
    private Season season;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String color;
}
