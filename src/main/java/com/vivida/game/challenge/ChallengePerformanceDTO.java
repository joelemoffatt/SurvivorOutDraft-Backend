package com.vivida.game.challenge;

/**
 * DTO for ChallengePerformance response - breaks circular references
 */
public class ChallengePerformanceDTO {
    public Integer id;
    public Integer challengeId;
    public Integer castawayId;
    public String castawayName;
    public Integer place;
    public Boolean satOut;
    public Boolean won;

    public ChallengePerformanceDTO(ChallengePerformance performance) {
        this.id = performance.getId();
        this.place = performance.getPlace();
        this.satOut = performance.getSatOut();
        this.won = performance.getWon();
        
        if (performance.getChallenge() != null) {
            this.challengeId = performance.getChallenge().getId();
        }
        
        if (performance.getCastaway() != null) {
            this.castawayId = performance.getCastaway().getId();
            if (performance.getCastaway().getCastaway() != null) {
                this.castawayName = performance.getCastaway().getCastaway().getName();
            }
        }
    }
}
