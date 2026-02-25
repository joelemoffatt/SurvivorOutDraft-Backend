package com.vivida.game.castaway;

/**
 * DTO for CastawayPerformance response - breaks circular references
 */
public class CastawayPerformanceDTO {
    public Integer id;
    public SeasonDTO season;
    public CastawayDTO castaway;

    public CastawayPerformanceDTO(CastawayPerformance performance) {
        this.id = performance.getId();
        
        if (performance.getSeason() != null) {
            this.season = new SeasonDTO(performance.getSeason());
        }
        
        if (performance.getCastaway() != null) {
            this.castaway = new CastawayDTO(performance.getCastaway());
        }
    }

    public static class SeasonDTO {
        public Integer id;
        public String seasonName;
        public String version;

        public SeasonDTO(com.vivida.game.season.Season season) {
            this.id = season.getSeason();
            this.seasonName = season.getSeasonName();
            this.version = season.getVersion();
        }
    }

    public static class CastawayDTO {
        public Integer id;
        public String name;
        public String full_name;

        public CastawayDTO(Castaway castaway) {
            this.id = castaway.getId();
            this.name = castaway.getName();
            this.full_name = castaway.getFull_name();
        }
    }
}
