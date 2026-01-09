package com.vivida;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.util.Objects;

@Entity
public class Castaway {

    @Id
    private Integer id;
    private String name;
    private Integer seasonNumber;
    private Integer finalPlacement;

    private Integer ageAtPlay;
    private Integer currentAge;
    private String birthDate;
    private String occupation;
    private String hometown;
    private String gender;
    private boolean lgbt;
    private String description;

    private Integer seasonsPlayed;
    private Integer wins;

    private Integer overallScorePercent;
    private Integer overallRank;

    private Integer challengesScorePercent;
    private Integer advantagesScorePercent;
    private Integer influenceScorePercent;
    private Integer tribalCouncilScorePercent;
    private Integer outlastScorePercent;
    private Integer juryScorePercent;

    public Castaway(Integer id,
                    String name,
                    Integer seasonNumber,
                    Integer finalPlacement,
                    Integer ageAtPlay,
                    Integer currentAge,
                    String birthDate,
                    String occupation,
                    String hometown,
                    String gender,
                    String ethnicity,
                    boolean lgbt,
                    String description,
                    String personalityType,
                    Integer seasonsPlayed,
                    Integer wins,
                    Integer overallScorePercent,
                    Integer overallRank,
                    Integer challengesScorePercent,
                    Integer advantagesScorePercent,
                    Integer influenceScorePercent,
                    Integer tribalCouncilScorePercent,
                    Integer outlastScorePercent,
                    Integer juryScorePercent) {
        this.id = id;
        this.name = name;
        this.seasonNumber = seasonNumber;
        this.finalPlacement = finalPlacement;
        this.ageAtPlay = ageAtPlay;
        this.currentAge = currentAge;
        this.birthDate = birthDate;
        this.occupation = occupation;
        this.hometown = hometown;
        this.gender = gender;
        this.lgbt = lgbt;
        this.description = description;
        this.seasonsPlayed = seasonsPlayed;
        this.wins = wins;
        this.overallScorePercent = overallScorePercent;
        this.overallRank = overallRank;
        this.challengesScorePercent = challengesScorePercent;
        this.advantagesScorePercent = advantagesScorePercent;
        this.influenceScorePercent = influenceScorePercent;
        this.tribalCouncilScorePercent = tribalCouncilScorePercent;
        this.outlastScorePercent = outlastScorePercent;
        this.juryScorePercent = juryScorePercent;
    }

    public Castaway() {

    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getSeasonNumber() {
        return seasonNumber;
    }

    public void setSeasonNumber(Integer seasonNumber) {
        this.seasonNumber = seasonNumber;
    }

    public Integer getFinalPlacement() {
        return finalPlacement;
    }

    public void setFinalPlacement(Integer finalPlacement) {
        this.finalPlacement = finalPlacement;
    }

    public Integer getAgeAtPlay() {
        return ageAtPlay;
    }

    public void setAgeAtPlay(Integer ageAtPlay) {
        this.ageAtPlay = ageAtPlay;
    }

    public Integer getCurrentAge() {
        return currentAge;
    }

    public void setCurrentAge(Integer currentAge) {
        this.currentAge = currentAge;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public String getHometown() {
        return hometown;
    }

    public void setHometown(String hometown) {
        this.hometown = hometown;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public boolean isLgbt() {
        return lgbt;
    }

    public void setLgbt(boolean lgbt) {
        this.lgbt = lgbt;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getSeasonsPlayed() {
        return seasonsPlayed;
    }

    public void setSeasonsPlayed(Integer seasonsPlayed) {
        this.seasonsPlayed = seasonsPlayed;
    }

    public Integer getWins() {
        return wins;
    }

    public void setWins(Integer wins) {
        this.wins = wins;
    }

    public Integer getOverallScorePercent() {
        return overallScorePercent;
    }

    public void setOverallScorePercent(Integer overallScorePercent) {
        this.overallScorePercent = overallScorePercent;
    }

    public Integer getOverallRank() {
        return overallRank;
    }

    public void setOverallRank(Integer overallRank) {
        this.overallRank = overallRank;
    }

    public Integer getChallengesScorePercent() {
        return challengesScorePercent;
    }

    public void setChallengesScorePercent(Integer challengesScorePercent) {
        this.challengesScorePercent = challengesScorePercent;
    }

    public Integer getAdvantagesScorePercent() {
        return advantagesScorePercent;
    }

    public void setAdvantagesScorePercent(Integer advantagesScorePercent) {
        this.advantagesScorePercent = advantagesScorePercent;
    }

    public Integer getInfluenceScorePercent() {
        return influenceScorePercent;
    }

    public void setInfluenceScorePercent(Integer influenceScorePercent) {
        this.influenceScorePercent = influenceScorePercent;
    }

    public Integer getTribalCouncilScorePercent() {
        return tribalCouncilScorePercent;
    }

    public void setTribalCouncilScorePercent(Integer tribalCouncilScorePercent) {
        this.tribalCouncilScorePercent = tribalCouncilScorePercent;
    }

    public Integer getOutlastScorePercent() {
        return outlastScorePercent;
    }

    public void setOutlastScorePercent(Integer outlastScorePercent) {
        this.outlastScorePercent = outlastScorePercent;
    }

    public Integer getJuryScorePercent() {
        return juryScorePercent;
    }

    public void setJuryScorePercent(Integer juryScorePercent) {
        this.juryScorePercent = juryScorePercent;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Castaway castaway = (Castaway) o;
        return lgbt == castaway.lgbt && Objects.equals(id, castaway.id) && Objects.equals(name, castaway.name) && Objects.equals(seasonNumber, castaway.seasonNumber) && Objects.equals(finalPlacement, castaway.finalPlacement) && Objects.equals(ageAtPlay, castaway.ageAtPlay) && Objects.equals(currentAge, castaway.currentAge) && Objects.equals(birthDate, castaway.birthDate) && Objects.equals(occupation, castaway.occupation) && Objects.equals(hometown, castaway.hometown) && Objects.equals(gender, castaway.gender) && Objects.equals(description, castaway.description) && Objects.equals(seasonsPlayed, castaway.seasonsPlayed) && Objects.equals(wins, castaway.wins) && Objects.equals(overallScorePercent, castaway.overallScorePercent) && Objects.equals(overallRank, castaway.overallRank) && Objects.equals(challengesScorePercent, castaway.challengesScorePercent) && Objects.equals(advantagesScorePercent, castaway.advantagesScorePercent) && Objects.equals(influenceScorePercent, castaway.influenceScorePercent) && Objects.equals(tribalCouncilScorePercent, castaway.tribalCouncilScorePercent) && Objects.equals(outlastScorePercent, castaway.outlastScorePercent) && Objects.equals(juryScorePercent, castaway.juryScorePercent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, seasonNumber, finalPlacement, ageAtPlay, currentAge, birthDate, occupation, hometown, gender, lgbt, description, seasonsPlayed, wins, overallScorePercent, overallRank, challengesScorePercent, advantagesScorePercent, influenceScorePercent, tribalCouncilScorePercent, outlastScorePercent, juryScorePercent);
    }
}
