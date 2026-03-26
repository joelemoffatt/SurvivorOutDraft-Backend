package com.vivida.social.team;

import com.vivida.auth.User;
import com.vivida.scoring.ScoreBreakdownDTO;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @PutMapping(value = "{id}/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public TeamDTO updateTeamProfile(
            @PathVariable Integer id,
            @RequestPart(value = "teamName", required = false) String teamName,
            @RequestPart(value = "file", required = false) MultipartFile file,
            Authentication authentication) {
        User requestingUser = (User) authentication.getPrincipal();
        return teamService.updateTeamProfile(id, requestingUser, teamName, file);
    }

    @GetMapping("{id}/avatar")
    public ResponseEntity<byte[]> downloadAvatar(@PathVariable Integer id) {
        TeamAvatar avatar = teamService.getAvatarByTeamId(id);
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        String contentType = avatar.getContentType();
        if (contentType != null) {
            mediaType = MediaType.parseMediaType(contentType);
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(avatar.getImageData());
    }

    @DeleteMapping("{id}/avatar")
    public void deleteAvatar(@PathVariable Integer id, Authentication authentication) {
        User requestingUser = (User) authentication.getPrincipal();
        teamService.deleteAvatar(id, requestingUser);
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

