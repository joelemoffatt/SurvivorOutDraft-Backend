package com.vivida.social.team;

import com.vivida.auth.Role;
import com.vivida.auth.User;
import com.vivida.game.boot.Boot;
import com.vivida.game.boot.BootRepository;
import com.vivida.scoring.ScoreBreakdownDTO;
import com.vivida.scoring.TeamCastawayScoreEvent;
import com.vivida.scoring.TeamCastawayScoreEventRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamCastawayRepository teamCastawayRepository;
    private final TeamCastawayScoreEventRepository scoreEventRepository;
    private final BootRepository bootRepository;
        private final TeamAvatarRepository teamAvatarRepository;

    private final Map<String, Map<Integer, String>> bootPlacementCache = new ConcurrentHashMap<>();
    private final Map<Integer, String> avatarImageUrlCache = new ConcurrentHashMap<>();

    private static final long MAX_AVATAR_FILE_SIZE_BYTES = 5L * 1024L * 1024L;
    private static final Set<String> ALLOWED_AVATAR_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif"
    );

    public TeamService(
            TeamRepository teamRepository,
            TeamCastawayRepository teamCastawayRepository,
            TeamCastawayScoreEventRepository scoreEventRepository,
            BootRepository bootRepository,
            TeamAvatarRepository teamAvatarRepository) {
        this.teamRepository = teamRepository;
        this.teamCastawayRepository = teamCastawayRepository;
        this.scoreEventRepository = scoreEventRepository;
        this.bootRepository = bootRepository;
        this.teamAvatarRepository = teamAvatarRepository;
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
        TeamDTO dto = toTeamDTO(team);
        populateRosterPlacement(team, dto);
        return dto;
    }

    public List<TeamDTO> getTeamDtosByGroupId(int groupId) {
        List<com.vivida.scoring.TeamCastawayScoreEvent> allEvents = scoreEventRepository.findByGroupId(groupId);
        Map<Integer, List<com.vivida.scoring.TeamCastawayScoreEvent>> eventsByTcId = new HashMap<>();
        for (com.vivida.scoring.TeamCastawayScoreEvent event : allEvents) {
            Integer tcId = event.getTeamCastaway() != null ? event.getTeamCastaway().getId() : null;
            if (tcId != null) {
                eventsByTcId.computeIfAbsent(tcId, k -> new ArrayList<>()).add(event);
            }
        }

        return getTeamsByGroupId(groupId).stream()
                .map(team -> {
                    TeamDTO dto = toTeamDTO(team);
                    populateRosterPlacement(team, dto);
                    populateRosterScoreEvents(dto, eventsByTcId);
                    return dto;
                })
                .toList();
    }

    private void populateRosterScoreEvents(TeamDTO dto,
            Map<Integer, List<com.vivida.scoring.TeamCastawayScoreEvent>> eventsByTcId) {
        if (dto.roster == null) return;
        for (TeamCastawayDTO castaway : dto.roster) {
            castaway.scoreEvents = eventsByTcId.getOrDefault(castaway.id, List.of())
                    .stream()
                    .sorted(Comparator.comparingInt(e -> e.getEpisodeNumber() != null ? e.getEpisodeNumber() : 0))
                    .map(TeamCastawayDTO.ScoreEventDTO::new)
                    .toList();
        }
    }

    public List<TeamDTO> getTeamDtosByUserId(int userId) {
        return getTeamsByUserId(userId).stream()
                .map(team -> {
                    TeamDTO dto = toTeamDTO(team);
                    populateRosterPlacement(team, dto);
                    return dto;
                })
                .toList();
    }

    public TeamDTO getTeamDtoByGroupAndUser(int groupId, int userId) {
        Team team = getTeamByGroupAndUser(groupId, userId);
        TeamDTO dto = toTeamDTO(team);
        populateRosterPlacement(team, dto);
        return dto;
    }

    public List<TeamDTO> getAllTeamDtos() {
        return getAllTeams().stream()
                .map(team -> {
                    TeamDTO dto = toTeamDTO(team);
                    populateRosterPlacement(team, dto);
                    return dto;
                })
                .toList();
    }

    @Transactional
    public TeamDTO updateTeamProfile(int teamId, User requestingUser, String teamName, MultipartFile avatarFile) {
        Team team = getTeamById(teamId);

        if (!team.getUser().getId().equals(requestingUser.getId()) && requestingUser.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only update your own team");
        }

        boolean hasTeamNameUpdate = teamName != null;
        boolean hasAvatarUpdate = avatarFile != null && !avatarFile.isEmpty();
        if (!hasTeamNameUpdate && !hasAvatarUpdate) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Provide a teamName or avatar file to update");
        }

        if (hasTeamNameUpdate) {
            String normalizedTeamName = teamName.trim();
            if (normalizedTeamName.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Team name cannot be blank");
            }
            if (normalizedTeamName.length() > 100) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Team name must be 100 characters or fewer");
            }
            team.setTeamName(normalizedTeamName);
            teamRepository.save(team);
        }

        if (hasAvatarUpdate) {
            upsertTeamAvatar(team, avatarFile);
        }

        return getTeamDtoById(teamId);
    }

    @Transactional(readOnly = true)
    public TeamAvatar getAvatarByTeamId(int teamId) {
        return teamAvatarRepository.findByTeamId(teamId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Avatar not found"));
    }

    @Transactional
    public void deleteAvatar(int teamId, User requestingUser) {
        Team team = getTeamById(teamId);
        if (!team.getUser().getId().equals(requestingUser.getId()) && requestingUser.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only delete your own team avatar");
        }

        TeamAvatar avatar = teamAvatarRepository.findByTeamId(teamId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Avatar not found"));
        teamAvatarRepository.delete(avatar);
    }

    public String getAvatarImageUrl(Integer teamId) {
        if (teamId == null) {
            return null;
        }
        String cached = avatarImageUrlCache.get(teamId);
        if (cached != null) {
            return cached.isEmpty() ? null : cached;
        }

        String resolved = teamAvatarRepository.findByTeamId(teamId)
            .map(avatar -> TeamAvatarImageUrlResolver.buildAvatarUrl(teamId))
            .orElse(null);
        avatarImageUrlCache.put(teamId, resolved == null ? "" : resolved);
        return resolved;
    }

    private void upsertTeamAvatar(Team team, MultipartFile avatarFile) {
        if (avatarFile.getSize() > MAX_AVATAR_FILE_SIZE_BYTES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Avatar file size must be 5MB or less");
        }

        String contentType = avatarFile.getContentType();
        if (contentType == null || !ALLOWED_AVATAR_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only JPG, PNG, WEBP, and GIF avatars are supported");
        }

        TeamAvatar avatar = teamAvatarRepository.findByTeamId(team.getId())
                .orElseGet(() -> {
                    TeamAvatar created = new TeamAvatar();
                    created.setTeam(team);
                    return created;
                });

        try {
            avatar.setImageData(avatarFile.getBytes());
            avatar.setContentType(contentType);
            teamAvatarRepository.save(avatar);
            avatarImageUrlCache.remove(team.getId());
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to read avatar upload", ex);
        }
    }

    private TeamDTO toTeamDTO(Team team) {
        return new TeamDTO(team, getAvatarImageUrl(team.getId()));
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

        String cacheKey = seasonId + ":" + (latestEpisodeNumber != null ? latestEpisodeNumber : "all");
        Map<Integer, String> placementByPerformanceId = bootPlacementCache.computeIfAbsent(cacheKey, key -> {
            List<Boot> boots = latestEpisodeNumber != null
                    ? bootRepository.findBySeasonIdAndEpisodeNumberLessThanEqual(seasonId, latestEpisodeNumber)
                    : bootRepository.findBySeasonId(seasonId);
            boots.sort(
                Comparator.comparingInt((Boot boot) ->
                        boot.getEpisode() != null && boot.getEpisode().getEpisodeNumber() != null
                            ? boot.getEpisode().getEpisodeNumber()
                            : 0)
                    .reversed());

            Map<Integer, String> placements = new HashMap<>();
            for (Boot boot : boots) {
                Integer performanceId = boot.getCastaway() != null ? boot.getCastaway().getId() : null;
                if (performanceId == null || placements.containsKey(performanceId)) {
                    continue;
                }
                placements.put(performanceId, derivePlacementFromBootEvent(boot.getEvent()));
            }
            return placements;
        });

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
        List<TeamCastawayScoreEvent> events = scoreEventRepository.findByTeamId(teamId);

        Map<Integer, List<TeamCastawayScoreEvent>> eventsByCastawayId = new HashMap<>();
        for (TeamCastawayScoreEvent event : events) {
            Integer castawayId = event.getTeamCastaway() != null ? event.getTeamCastaway().getId() : null;
            if (castawayId == null) {
                continue;
            }
            eventsByCastawayId.computeIfAbsent(castawayId, key -> new ArrayList<>()).add(event);
        }

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

                List<TeamCastawayScoreEvent> castawayEvents = eventsByCastawayId.getOrDefault(tc.getId(), List.of());
                cb.scoreEvents = castawayEvents.stream()
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
