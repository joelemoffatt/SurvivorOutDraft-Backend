package com.vivida.game.season;

/**
 * DTO for Season response - minimal to prevent circular references
 */
public class SeasonDTO {
    public Integer season;
    public String version;
    public String seasonName;
    public String location;
    public String country;
    public String tribeSetup;
    public String fullName;
    public String filmingStarted;
    public String filmingEnded;
    public String premiereDate;
    public String endingDate;
    public Integer viewers;
    public Integer episodesNumber;

    public SeasonDTO(Season season) {
        this.season = season.getSeason();
        this.version = season.getVersion();
        this.seasonName = season.getSeasonName();
        this.location = season.getLocation();
        this.country = season.getCountry();
        this.tribeSetup = season.getTribeSetup();
        this.fullName = season.getFullName();
        this.filmingStarted = season.getFilmingStarted();
        this.filmingEnded = season.getFilmingEnded();
        this.premiereDate = season.getPremiereDate();
        this.endingDate = season.getEndingDate();
        this.viewers = season.getViewers();
        this.episodesNumber = season.getEpisodesNumber();
    }
}
