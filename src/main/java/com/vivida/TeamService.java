package com.vivida;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamCastawayRepository teamCastawayRepository;

    public TeamService(TeamRepository teamRepository, TeamCastawayRepository teamCastawayRepository) {
        this.teamRepository = teamRepository;
        this.teamCastawayRepository = teamCastawayRepository;
    }

    public List<Team> getAllTeams() {
        return teamRepository.findAll();
    }

    public Team getTeamById(int id) {
        return teamRepository.findById(id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Team not found with id " + id
        ));
    }

    public List<Team> getTeamsByGroupId(int groupId) {
        return teamRepository.findByGroupId(groupId);
    }

    public List<Team> getTeamsByUserId(int userId) {
        return teamRepository.findByUserId(userId);
    }

    public Team getTeamByGroupAndUser(int groupId, int userId) {
        return teamRepository.findByGroupIdAndUserId(groupId, userId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Team not found for group " + groupId + " and user " + userId)
        );
    }

    public void insertTeam(Team team) {
        if (teamRepository.existsByGroupIdAndUserId(
                team.getGroup().getId(),
                team.getUser().getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User already has a team in this group");
        }
        teamRepository.save(team);
    }

    public void updateTeam(Team team) {
        teamRepository.save(team);
    }

    public void recalculateTeamPoints(int teamId) {
        Team team = getTeamById(teamId);
        List<TeamCastaway> roster = teamCastawayRepository.findByTeamId(teamId);
        int totalPoints = roster.stream().mapToInt(TeamCastaway::getPoints).sum();
        team.setTotalPoints(totalPoints);
        teamRepository.save(team);
    }

    public void deleteTeamById(int id) {
        teamRepository.deleteById(id);
    }
}
