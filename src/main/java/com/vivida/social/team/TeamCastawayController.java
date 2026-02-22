package com.vivida.social.team;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/team-castaways")
public class TeamCastawayController {

    private final TeamCastawayService teamCastawayService;

    public TeamCastawayController(TeamCastawayService teamCastawayService) {
        this.teamCastawayService = teamCastawayService;
    }

    @GetMapping
    public List<TeamCastaway> getTeamCastaways() {
        return teamCastawayService.getAllTeamCastaways();
    }

    @GetMapping("{id}")
    public TeamCastaway getTeamCastawayById(@PathVariable Integer id) {
        return teamCastawayService.getTeamCastawayById(id);
    }

    @GetMapping("team/{teamId}")
    public List<TeamCastaway> getTeamCastawaysByTeamId(@PathVariable Integer teamId) {
        return teamCastawayService.getTeamCastawaysByTeamId(teamId);
    }

    @PostMapping
    public void draftCastaway(@RequestBody TeamCastaway teamCastaway) {
        teamCastawayService.draftCastaway(teamCastaway);
    }

    @PutMapping
    public void updateTeamCastaway(@RequestBody TeamCastaway teamCastaway) {
        teamCastawayService.updateTeamCastaway(teamCastaway);
    }

    @DeleteMapping("{id}")
    public void deleteTeamCastaway(@PathVariable Integer id) {
        teamCastawayService.deleteTeamCastawayById(id);
    }
}
