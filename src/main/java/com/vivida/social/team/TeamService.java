package com.vivida.social.team;

import com.vivida.scoring.ScoreBreakdownDTO;
import com.vivida.scoring.TeamCastawayScoreEvent;
import com.vivida.scoring.TeamCastawayScoreEventRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;

@Service
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamCastawayRepository teamCastawayRepository;
    private final TeamCastawayScoreEventRepository scoreEventRepository;

    public TeamService(
            TeamRepository teamRepository,
            TeamCastawayRepository teamCastawayRepository,
            TeamCastawayScoreEventRepository scoreEventRepository) {
        this.teamRepository = teamRepository;
        this.teamCastawayRepository = teamCastawayRepository;
        this.scoreEventRepository = scoreEventRepository;
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

    public ScoreBreakdownDTO getScoreBreakdown(int teamId) {
        Team team = getTeamById(teamId);
        List<TeamCastaway> roster = teamCastawayRepository.findByTeamId(teamId);

        ScoreBreakdownDTO dto = new ScoreBreakdownDTO();
        dto.teamId = team.getId();
        dto.teamName = team.getTeamName();
        dto.totalPoints = team.getTotalPoints();
        dto.castaways = roster.stream()
            .sorted(Comparator.comparing(tc -> tc.getDraftOrder() != null ? tc.getDraftOrder() : 0))
            .map(tc -> {
                ScoreBreakdownDTO.CastawayBreakdown cb = new ScoreBreakdownDTO.CastawayBreakdown();
                cb.teamCastawayId = tc.getId();
                cb.castawayPerformanceId = tc.getCastawayPerformance().getId();
                cb.castawayName = tc.getCastawayPerformance().getCastaway().getName();
                cb.totalPoints = tc.getPoints();

                List<TeamCastawayScoreEvent> events = scoreEventRepository.findByTeamCastawayId(tc.getId());
                cb.scoreEvents = events.stream()
                    .sorted(Comparator.comparing(e -> e.getEpisodeNumber() != null ? e.getEpisodeNumber() : 0))
                    .map(e -> {
                        ScoreBreakdownDTO.ScoreEventDTO sed = new ScoreBreakdownDTO.ScoreEventDTO();
                        sed.id = e.getId();
                        sed.episodeNumber = e.getEpisodeNumber();
                        sed.eventLabel = e.getEventLabel();
                        sed.totalPoints = e.getTotalPoints();
                        return sed;
                    })
                    .toList();
                return cb;
            })
            .toList();
        return dto;
    }
}
