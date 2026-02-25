package com.vivida.game.journey;

/**
 * DTO for Journey response - breaks circular references
 */
public class JourneyDTO {
    public Integer id;
    public Integer episodeId;
    public Integer castawayId;
    public String castawayName;
    public String reward;
    public Boolean lostVote;
    public Boolean choseToPlay;
    public String event;

    public JourneyDTO(Journey journey) {
        this.id = journey.getId();
        this.reward = journey.getReward();
        this.lostVote = journey.getLostVote();
        this.choseToPlay = journey.getChoseToPlay();
        this.event = journey.getEvent();
        
        if (journey.getEpisode() != null) {
            this.episodeId = journey.getEpisode().getId();
        }
        
        if (journey.getCastaway() != null) {
            this.castawayId = journey.getCastaway().getId();
            if (journey.getCastaway().getCastaway() != null) {
                this.castawayName = journey.getCastaway().getCastaway().getName();
            }
        }
    }
}
