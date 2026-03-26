package com.vivida.social.team;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO for Team response - breaks circular references
 */
public class TeamDTO {
    public Integer id;
    public String teamName;
    public String avatarImage;
    public Integer totalPoints;
    public LocalDateTime createdAt;
    public List<TeamCastawayDTO> roster;

    public TeamDTO(Team team, String avatarImage) {
        this.id = team.getId();
        this.teamName = team.getTeamName();
        this.avatarImage = avatarImage;
        this.totalPoints = team.getTotalPoints();
        this.createdAt = team.getCreatedAt();
        this.roster = team.getRoster() != null 
            ? team.getRoster().stream()
                .map(TeamCastawayDTO::new)
                .collect(Collectors.toList())
            : List.of();
    }
}
