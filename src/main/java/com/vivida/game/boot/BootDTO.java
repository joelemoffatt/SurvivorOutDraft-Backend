package com.vivida.game.boot;

/**
 * DTO for Boot response - breaks circular references
 */
public class BootDTO {
    public Integer id;
    public Integer episodeId;
    public Integer tribalId;
    public Integer castawayId;
    public String castawayName;
    public Integer bootOrder;
    public String event;

    public BootDTO(Boot boot) {
        this.id = boot.getId();
        this.bootOrder = boot.getBootOrder();
        this.event = boot.getEvent();
        
        if (boot.getEpisode() != null) {
            this.episodeId = boot.getEpisode().getId();
        }
        
        if (boot.getTribal() != null) {
            this.tribalId = boot.getTribal().getId();
        }
        
        if (boot.getCastaway() != null) {
            this.castawayId = boot.getCastaway().getId();
            if (boot.getCastaway().getCastaway() != null) {
                this.castawayName = boot.getCastaway().getCastaway().getName();
            }
        }
    }
}
