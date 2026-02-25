package com.vivida.game.episode;

/**
 * DTO for Episode response - breaks circular references
 */
public class EpisodeDTO {
    public Integer id;
    public SeasonDTO season;
    public Integer episodeNumber;
    public String episodeTitle;
    public String episodeDate;
    public Integer episodeLength;
    public Boolean isFinale;

    public EpisodeDTO(Episode episode) {
        this.id = episode.getId();
        this.episodeNumber = episode.getEpisodeNumber();
        this.episodeTitle = episode.getEpisodeTitle();
        this.episodeDate = episode.getEpisodeDate();
        this.episodeLength = episode.getEpisodeLength();
        this.isFinale = episode.getIsFinale();
        
        if (episode.getSeason() != null) {
            this.season = new SeasonDTO(episode.getSeason());
        }
    }

    public static class SeasonDTO {
        public Integer season;
        public String version;
        public String seasonName;

        public SeasonDTO(com.vivida.game.season.Season season) {
            this.season = season.getSeason();
            this.version = season.getVersion();
            this.seasonName = season.getSeasonName();
        }
    }
}
