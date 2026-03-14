package com.vivida.game.tribe;

/**
 * DTO for TribeMapping response - breaks circular references
 */
public class TribeMappingDTO {
    public Integer id;
    public Integer seasonId;
    public Integer episodeId;
    public Integer episodeNumber;
    public Integer castawayPerformanceId;
    public Integer castawayId;
    public String castawayJsonId;
    public String castawayName;
    public Integer tribeId;
    public String tribeName;
    public String status;

    public TribeMappingDTO(TribeMapping mapping) {
        this.id = mapping.getId();
        this.status = mapping.getStatus();
        
        if (mapping.getSeason() != null) {
            this.seasonId = mapping.getSeason().getSeason();
        }
        
        if (mapping.getEpisode() != null) {
            this.episodeId = mapping.getEpisode().getId();
            this.episodeNumber = mapping.getEpisode().getEpisodeNumber();
        }
        
        if (mapping.getCastawayPerformance() != null) {
            this.castawayPerformanceId = mapping.getCastawayPerformance().getId();
            if (mapping.getCastawayPerformance().getCastaway() != null) {
                this.castawayId = mapping.getCastawayPerformance().getCastaway().getId();
                this.castawayJsonId = mapping.getCastawayPerformance().getCastaway().getJson_id();
                this.castawayName = mapping.getCastawayPerformance().getCastaway().getName();
            }
        }
        
        if (mapping.getTribe() != null) {
            this.tribeId = mapping.getTribe().getId();
            this.tribeName = mapping.getTribe().getName();
        }
    }
}
