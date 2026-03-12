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
        return teamService.getAllTeams().stream()
                .map(TeamDTO::new)
                .toList();
    }

    @GetMapping("{id}")
    public TeamDTO getTeamById(@PathVariable Integer id) {
        return new TeamDTO(teamService.getTeamById(id));
    }

    @GetMapping("group/{groupId}")
    public List<TeamDTO> getTeamsByGroupId(@PathVariable Integer groupId) {
        return teamService.getTeamsByGroupId(groupId).stream()
                .map(TeamDTO::new)
                .toList();
    }

    @GetMapping("user/{userId}")
    public List<TeamDTO> getTeamsByUserId(@PathVariable Integer userId) {
        return teamService.getTeamsByUserId(userId).stream()
                .map(TeamDTO::new)
                .toList();
    }

    @GetMapping("group/{groupId}/user/{userId}")
    public TeamDTO getTeamByGroupAndUser(
            @PathVariable Integer groupId,
            @PathVariable Integer userId) {
        return new TeamDTO(teamService.getTeamByGroupAndUser(groupId, userId));
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

