package com.vivida.game.challenge;

/**
 * DTO for Challenge response - breaks circular references
 */
public class ChallengeDTO {
    public Integer id;
    public Integer seasonId;
    public Integer episodeId;
    public Integer challenge_id;
    public Integer challenge_number;
    public String challenge_type;
    public String name;
    public Boolean balance;
    public Boolean endurance;
    public Boolean puzzle;
    public Boolean precision;
    public Boolean water;

    public ChallengeDTO(Challenge challenge) {
        this.id = challenge.getId();
        this.challenge_id = challenge.getChallenge_id();
        this.challenge_number = challenge.getChallenge_number();
        this.challenge_type = challenge.getChallenge_type();
        this.name = challenge.getName();
        this.balance = challenge.getBalance();
        this.endurance = challenge.getEndurance();
        this.puzzle = challenge.getPuzzle();
        this.precision = challenge.getPrecision();
        this.water = challenge.getWater();
        
        if (challenge.getSeason() != null) {
            this.seasonId = challenge.getSeason().getSeason();
        }
        
        if (challenge.getEpisode() != null) {
            this.episodeId = challenge.getEpisode().getId();
        }
    }
}
