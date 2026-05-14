package com.vivida.social.team;

import com.vivida.scoring.TeamCastawayScoreEvent;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for TeamCastaway response - breaks circular references
 */
public class TeamCastawayDTO {
    public Integer id;
    public Integer draftOrder;
    public Integer points;
    public LocalDateTime draftedAt;
    public String placement;
    public List<ScoreEventDTO> scoreEvents = new ArrayList<>();
    public CastawayPerformanceDTO castawayPerformance;

    public TeamCastawayDTO(TeamCastaway teamCastaway) {
        this.id = teamCastaway.getId();
        this.draftOrder = teamCastaway.getDraftOrder();
        this.points = teamCastaway.getPoints();
        this.draftedAt = teamCastaway.getDraftedAt();
        this.placement = null;
        this.castawayPerformance = new CastawayPerformanceDTO(teamCastaway.getCastawayPerformance());
    }

    public static class ScoreEventDTO {
        public Integer id;
        public Integer episodeNumber;
        public String eventLabel;
        public Integer totalPoints;

        public ScoreEventDTO(TeamCastawayScoreEvent event) {
            this.id = event.getId();
            this.episodeNumber = event.getEpisodeNumber();
            this.eventLabel = event.getEventLabel();
            this.totalPoints = event.getTotalPoints();
        }
    }

    public static class CastawayPerformanceDTO {
        public Integer id;
        public Integer seasonId;
        public CastawayDTO castaway;

        public CastawayPerformanceDTO(com.vivida.game.castaway.CastawayPerformance performance) {
            this.id = performance.getId();
            this.seasonId = performance.getSeason() != null ? performance.getSeason().getSeason() : null;
            this.castaway = new CastawayDTO(performance.getCastaway());
        }

        public static class CastawayDTO {
            public Integer id;
            public String json_id;
            public String name;
            public String full_name;

            public CastawayDTO(com.vivida.game.castaway.Castaway castaway) {
                this.id = castaway.getId();
                this.json_id = castaway.getJson_id();
                this.name = castaway.getName();
                this.full_name = castaway.getFull_name();
            }
        }
    }
}
