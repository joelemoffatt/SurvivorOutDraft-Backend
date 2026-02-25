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
    public List<TeamCastawayDTO> getTeamCastaways() {
        return teamCastawayService.getAllTeamCastaways().stream()
                .map(TeamCastawayDTO::new)
                .toList();
    }

    @GetMapping("{id}")
    public TeamCastawayDTO getTeamCastawayById(@PathVariable Integer id) {
        return new TeamCastawayDTO(teamCastawayService.getTeamCastawayById(id));
    }

    @GetMapping("team/{teamId}")
    public List<TeamCastawayDTO> getTeamCastawaysByTeamId(@PathVariable Integer teamId) {
        return teamCastawayService.getTeamCastawaysByTeamId(teamId).stream()
                .map(TeamCastawayDTO::new)
                .toList();
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
