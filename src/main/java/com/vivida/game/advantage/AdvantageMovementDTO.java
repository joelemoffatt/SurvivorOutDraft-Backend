package com.vivida.game.advantage;

/**
 * DTO for AdvantageMovement response - breaks circular references
 */
public class AdvantageMovementDTO {
    public Integer id;
    public Integer castawayId;
    public String castawayName;
    public Integer playedForId;
    public String playedForName;
    public Integer episodeId;
    public String event;
    public String advantageType;
    public String success;
    public Integer votesNullified;

    public AdvantageMovementDTO(AdvantageMovement movement) {
        this.id = movement.getId();
        this.event = movement.getEvent();
        this.advantageType = movement.getAdvantageType();
        this.success = movement.getSuccess();
        this.votesNullified = movement.getVotesNullified();
        
        if (movement.getCastawayId() != null) {
            this.castawayId = movement.getCastawayId().getId();
            if (movement.getCastawayId().getCastaway() != null) {
                this.castawayName = movement.getCastawayId().getCastaway().getName();
            }
        }
        
        if (movement.getPlayedForId() != null) {
            this.playedForId = movement.getPlayedForId().getId();
            if (movement.getPlayedForId().getCastaway() != null) {
                this.playedForName = movement.getPlayedForId().getCastaway().getName();
            }
        }
        
        if (movement.getEpisode() != null) {
            this.episodeId = movement.getEpisode().getId();
        }
    }
}
