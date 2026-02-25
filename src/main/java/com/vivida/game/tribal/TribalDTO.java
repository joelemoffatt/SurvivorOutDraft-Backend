package com.vivida.game.tribal;

/**
 * DTO for Tribal response - breaks circular references
 */
public class TribalDTO {
    public Integer id;
    public Integer episodeId;
    public Integer tribeId;
    public String tribeName;
    public Integer bootOrder;

    public TribalDTO(Tribal tribal) {
        this.id = tribal.getId();
        this.bootOrder = tribal.getBootOrder();
        
        if (tribal.getEpisode() != null) {
            this.episodeId = tribal.getEpisode().getId();
        }
        
        if (tribal.getTribe() != null) {
            this.tribeId = tribal.getTribe().getId();
            this.tribeName = tribal.getTribe().getName();
        }
    }
}
