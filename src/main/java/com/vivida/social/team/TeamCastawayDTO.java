package com.vivida.social.team;

import java.time.LocalDateTime;

/**
 * DTO for TeamCastaway response - breaks circular references
 */
public class TeamCastawayDTO {
    public Integer id;
    public Integer draftOrder;
    public Integer points;
    public LocalDateTime draftedAt;
    public CastawayPerformanceDTO castawayPerformance;

    public TeamCastawayDTO(TeamCastaway teamCastaway) {
        this.id = teamCastaway.getId();
        this.draftOrder = teamCastaway.getDraftOrder();
        this.points = teamCastaway.getPoints();
        this.draftedAt = teamCastaway.getDraftedAt();
        this.castawayPerformance = new CastawayPerformanceDTO(teamCastaway.getCastawayPerformance());
    }

    public static class CastawayPerformanceDTO {
        public Integer id;
        public CastawayDTO castaway;

        public CastawayPerformanceDTO(com.vivida.game.castaway.CastawayPerformance performance) {
            this.id = performance.getId();
            this.castaway = new CastawayDTO(performance.getCastaway());
        }

        public static class CastawayDTO {
            public Integer id;
            public String name;
            public String full_name;

            public CastawayDTO(com.vivida.game.castaway.Castaway castaway) {
                this.id = castaway.getId();
                this.name = castaway.getName();
                this.full_name = castaway.getFull_name();
            }
        }
    }
}
