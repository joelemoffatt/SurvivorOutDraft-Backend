package com.vivida.social.team;

import com.vivida.game.boot.Boot;
import com.vivida.game.boot.BootRepository;
import com.vivida.scoring.ScoreBreakdownDTO;
import com.vivida.scoring.TeamCastawayScoreEvent;
import com.vivida.scoring.TeamCastawayScoreEventRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamCastawayRepository teamCastawayRepository;
    private final TeamCastawayScoreEventRepository scoreEventRepository;
    private final BootRepository bootRepository;

    public TeamService(
            TeamRepository teamRepository,
            TeamCastawayRepository teamCastawayRepository,
            TeamCastawayScoreEventRepository scoreEventRepository,
            BootRepository bootRepository) {
        this.teamRepository = teamRepository;
        this.teamCastawayRepository = teamCastawayRepository;
        this.scoreEventRepository = scoreEventRepository;
        this.bootRepository = bootRepository;
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

    public TeamDTO getTeamDtoById(int id) {
        Team team = getTeamById(id);
        TeamDTO dto = new TeamDTO(team);
        populateRosterPlacement(team, dto);
        return dto;
    }

    public List<TeamDTO> getTeamDtosByGroupId(int groupId) {
        return getTeamsByGroupId(groupId).stream()
                .map(team -> {
                    TeamDTO dto = new TeamDTO(team);
                    populateRosterPlacement(team, dto);
                    return dto;
                })
                .toList();
    }

    public List<TeamDTO> getTeamDtosByUserId(int userId) {
        return getTeamsByUserId(userId).stream()
                .map(team -> {
                    TeamDTO dto = new TeamDTO(team);
                    populateRosterPlacement(team, dto);
                    return dto;
                })
                .toList();
    }

    public TeamDTO getTeamDtoByGroupAndUser(int groupId, int userId) {
        Team team = getTeamByGroupAndUser(groupId, userId);
        TeamDTO dto = new TeamDTO(team);
        populateRosterPlacement(team, dto);
        return dto;
    }

    public List<TeamDTO> getAllTeamDtos() {
        return getAllTeams().stream()
                .map(team -> {
                    TeamDTO dto = new TeamDTO(team);
                    populateRosterPlacement(team, dto);
                    return dto;
                })
                .toList();
    }

    private void populateRosterPlacement(Team team, TeamDTO dto) {
        if (dto.roster == null || dto.roster.isEmpty()) {
            return;
        }

        Integer seasonId = team.getGroup() != null && team.getGroup().getSeason() != null
                ? team.getGroup().getSeason().getSeason()
                : null;
        Integer latestEpisodeNumber = team.getGroup() != null && team.getGroup().getLatestEpisodeWatched() != null
                ? team.getGroup().getLatestEpisodeWatched().getEpisodeNumber()
                : null;

        if (seasonId == null) {
            return;
        }

        List<Boot> boots = latestEpisodeNumber != null
                ? bootRepository.findBySeasonIdAndEpisodeNumberLessThanEqual(seasonId, latestEpisodeNumber)
                : bootRepository.findBySeasonId(seasonId);

        boots.sort(
                Comparator.comparingInt((Boot boot) ->
                                boot.getEpisode() != null && boot.getEpisode().getEpisodeNumber() != null
                                        ? boot.getEpisode().getEpisodeNumber()
                                        : 0)
                        .reversed());

        Map<Integer, String> placementByPerformanceId = new HashMap<>();
        for (Boot boot : boots) {
            Integer performanceId = boot.getCastaway() != null ? boot.getCastaway().getId() : null;
            if (performanceId == null || placementByPerformanceId.containsKey(performanceId)) {
                continue;
            }
            placementByPerformanceId.put(performanceId, derivePlacementFromBootEvent(boot.getEvent()));
        }

        for (TeamCastawayDTO castaway : dto.roster) {
            Integer performanceId = castaway.castawayPerformance != null ? castaway.castawayPerformance.id : null;
            castaway.placement = performanceId != null ? placementByPerformanceId.get(performanceId) : null;
        }
    }

    private String derivePlacementFromBootEvent(String event) {
        if (event == null || event.isBlank()) {
            return "booted";
        }

        String normalized = event.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "first" -> "first";
            case "second" -> "second";
            case "third" -> "third";
            case "lostfire", "lostfinalfire" -> "lostFire";
            case "votedout", "quit", "medevac" -> "booted";
            default -> {
                if (normalized.contains("lost") && normalized.contains("fire")) {
                    yield "lostFire";
                }
                if (normalized.contains("first")) {
                    yield "first";
                }
                if (normalized.contains("second") || normalized.contains("runner")) {
                    yield "second";
                }
                if (normalized.contains("third") || normalized.contains("3rd")) {
                    yield "third";
                }
                yield "booted";
            }
        };
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
