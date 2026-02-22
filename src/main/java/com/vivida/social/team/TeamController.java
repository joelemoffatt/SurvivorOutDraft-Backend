package com.vivida.social.team;

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
    public List<Team> getTeams() {
        return teamService.getAllTeams();
    }

    @GetMapping("{id}")
    public Team getTeamById(@PathVariable Integer id) {
        return teamService.getTeamById(id);
    }

    @GetMapping("group/{groupId}")
    public List<Team> getTeamsByGroupId(@PathVariable Integer groupId) {
        return teamService.getTeamsByGroupId(groupId);
    }

    @GetMapping("user/{userId}")
    public List<Team> getTeamsByUserId(@PathVariable Integer userId) {
        return teamService.getTeamsByUserId(userId);
    }

    @GetMapping("group/{groupId}/user/{userId}")
    public Team getTeamByGroupAndUser(
            @PathVariable Integer groupId,
            @PathVariable Integer userId) {
        return teamService.getTeamByGroupAndUser(groupId, userId);
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
