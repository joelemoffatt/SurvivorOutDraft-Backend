package com.vivida.social.team;

import com.vivida.scoring.ScoreBreakdownDTO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/teams")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @GetMapping
    public List<TeamDTO> getTeams() {
        return teamService.getAllTeamDtos();
    }

    @GetMapping("{id}")
    public TeamDTO getTeamById(@PathVariable Integer id) {
        return teamService.getTeamDtoById(id);
    }

    @GetMapping("group/{groupId}")
    public List<TeamDTO> getTeamsByGroupId(@PathVariable Integer groupId) {
        return teamService.getTeamDtosByGroupId(groupId);
    }

    @GetMapping("user/{userId}")
    public List<TeamDTO> getTeamsByUserId(@PathVariable Integer userId) {
        return teamService.getTeamDtosByUserId(userId);
    }

    @GetMapping("group/{groupId}/user/{userId}")
    public TeamDTO getTeamByGroupAndUser(
            @PathVariable Integer groupId,
            @PathVariable Integer userId) {
        return teamService.getTeamDtoByGroupAndUser(groupId, userId);
    }

    @GetMapping("{id}/score-breakdown")
    public ScoreBreakdownDTO getTeamScoreBreakdown(@PathVariable Integer id) {
        return teamService.getScoreBreakdown(id);
    }

    @PostMapping
    public void addTeam(@RequestBody Team team) {
        teamService.insertTeam(team);
    }

    @PutMapping
    public void updateTeam(@RequestBody Team team) {
        teamService.updateTeam(team);
    }

    @PostMapping("{id}/recalculate-points")
    public void recalculateTeamPoints(@PathVariable Integer id) {
        teamService.recalculateTeamPoints(id);
    }

    @DeleteMapping("{id}")
    public void deleteTeam(@PathVariable Integer id) {
        teamService.deleteTeamById(id);
    }
}

