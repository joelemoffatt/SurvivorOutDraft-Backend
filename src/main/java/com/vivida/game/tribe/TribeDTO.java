package com.vivida.game.tribe;

/**
 * DTO for Tribe response - breaks circular references
 */
public class TribeDTO {
    public Integer id;
    public Integer seasonId;
    public String seasonName;
    public String name;
    public String color;

    public TribeDTO(Tribe tribe) {
        this.id = tribe.getId();
        this.name = tribe.getName();
        this.color = tribe.getColor();
        
        if (tribe.getSeason() != null) {
            this.seasonId = tribe.getSeason().getSeason();
            this.seasonName = tribe.getSeason().getSeasonName();
        }
    }
}
