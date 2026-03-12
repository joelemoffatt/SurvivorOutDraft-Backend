package com.vivida.social.group;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import com.vivida.draft.DraftStatus;
import com.vivida.draft.DraftRepository;
import com.vivida.draft.DraftStyle;
import com.vivida.game.episode.Episode;
import com.vivida.game.episode.EpisodeRepository;
import com.vivida.game.season.Season;
import com.vivida.game.season.SeasonRepository;
import com.vivida.scoring.ScoreProjectionService;
import com.vivida.social.team.Team;
import com.vivida.social.team.TeamRepository;

import java.util.List;

@Service
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final TeamRepository teamRepository;
    private final EpisodeRepository episodeRepository;
    private final SeasonRepository seasonRepository;
    private final DraftRepository draftRepository;
    private final ScoreProjectionService scoreProjectionService;

    public GroupService(GroupRepository groupRepository,
                        GroupMemberRepository groupMemberRepository,
                        TeamRepository teamRepository,
                        EpisodeRepository episodeRepository,
                        SeasonRepository seasonRepository,
                        DraftRepository draftRepository,
                        ScoreProjectionService scoreProjectionService) {
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.teamRepository = teamRepository;
        this.episodeRepository = episodeRepository;
        this.seasonRepository = seasonRepository;
        this.draftRepository = draftRepository;
        this.scoreProjectionService = scoreProjectionService;
    }

    public List<Group> getAllGroups() {
        return groupRepository.findAll();
    }

    public Group getGroupById(int id) {
        return groupRepository.findById(id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Group not found with id " + id
        ));
    }

    public List<Group> getGroupsByAdminId(int adminId) {
        return groupRepository.findByAdminId(adminId);
    }

    public List<Group> getGroupsBySeasonId(int seasonId) {
        return groupRepository.findBySeasonSeason(seasonId);
    }

    public List<Group> getGroupsByUserId(int userId) {
        List<GroupMember> memberships = groupMemberRepository.findByUserIdAndStatus(userId, MembershipStatus.ACCEPTED);
        return memberships.stream().map(GroupMember::getGroup).toList();
    }

    public void insertGroup(Group group) {
        groupRepository.save(group);
    }

    @Transactional
    public Group createGroupWithAdmin(Group group) {
        // Save the group first
        Group savedGroup = groupRepository.save(group);
        
        // Automatically add admin as an ACCEPTED member
        GroupMember adminMembership = new GroupMember();
        adminMembership.setGroup(savedGroup);
        adminMembership.setUser(savedGroup.getAdmin());
        adminMembership.setStatus(MembershipStatus.ACCEPTED);
        groupMemberRepository.save(adminMembership);
        
        // Automatically create a team for the admin
        Team adminTeam = new Team();
        adminTeam.setGroup(savedGroup);
        adminTeam.setUser(savedGroup.getAdmin());
        adminTeam.setTeamName("Team " + savedGroup.getAdmin().getUsername());
        adminTeam.setTotalPoints(0);
        teamRepository.save(adminTeam);
        
        return savedGroup;
    }

    public void updateGroup(Group group) {
        groupRepository.save(group);
    }

    @Transactional
    public Group updateLatestEpisodeWatched(Integer groupId, Integer episodeId) {
        if (episodeId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "episodeId is required");
        }

        Group group = getGroupById(groupId);

        com.vivida.game.episode.Episode episode = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Episode not found with id " + episodeId));

        if (episode.getSeason() == null
                || !episode.getSeason().getSeason().equals(group.getSeason().getSeason())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Episode must belong to the group's season");
        }

        group.setLatestEpisodeWatched(episode);
        Group updated = groupRepository.save(group);
        scoreProjectionService.recalculateGroupScores(updated);
        return updated;
    }

    @Transactional
    public Group updateFirstScoringEpisodeNumber(Integer groupId, Integer firstScoringEpisodeNumber) {
        if (firstScoringEpisodeNumber == null || firstScoringEpisodeNumber < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "firstScoringEpisodeNumber must be >= 1");
        }

        Group group = getGroupById(groupId);
        group.setFirstScoringEpisodeNumber(firstScoringEpisodeNumber);
        Group updated = groupRepository.save(group);
        scoreProjectionService.recalculateGroupScores(updated);
        return updated;
    }

    @Transactional
    public Group updateGroupSettings(Integer groupId, UpdateGroupSettingsRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required");
        }

        Group group = getGroupById(groupId);

        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Group name is required");
        }
        if (request.getTeamSize() == null || request.getTeamSize() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Team size must be a positive integer");
        }

        Integer firstScoringEpisodeNumber = request.getFirstScoringEpisodeNumber() != null
                ? request.getFirstScoringEpisodeNumber()
                : 1;
        DraftStyle style = request.getStyle() != null ? request.getStyle() : DraftStyle.SNAKE;
        if (firstScoringEpisodeNumber < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "firstScoringEpisodeNumber must be >= 1");
        }

        if (request.getSeasonId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "seasonId is required");
        }

        Season season = seasonRepository.findById(request.getSeasonId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Season not found with id " + request.getSeasonId()));

        Episode latestWatchedEpisode = null;
        if (request.getLatestWatchedEpisodeId() != null) {
            latestWatchedEpisode = episodeRepository.findById(request.getLatestWatchedEpisodeId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Episode not found with id " + request.getLatestWatchedEpisodeId()));

            if (latestWatchedEpisode.getSeason() == null
                    || !latestWatchedEpisode.getSeason().getSeason().equals(season.getSeason())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Latest watched episode must belong to the selected season");
            }

            if (firstScoringEpisodeNumber > latestWatchedEpisode.getEpisodeNumber()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "firstScoringEpisodeNumber cannot be greater than latest watched episode number");
            }
        }

        group.setName(request.getName().trim());
        group.setSeason(season);
        group.setTeamSize(request.getTeamSize());
        group.setLatestEpisodeWatched(latestWatchedEpisode);
        group.setFirstScoringEpisodeNumber(firstScoringEpisodeNumber);

        Group updated = groupRepository.save(group);

        // Keep pending draft configuration in sync with editable group settings.
        draftRepository.findByGroupIdAndStatus(groupId, DraftStatus.PENDING).ifPresent(draft -> {
            draft.setSeason(season);
            draft.setTeamSize(request.getTeamSize());
            draft.setStyle(style);
            draftRepository.save(draft);
        });

        scoreProjectionService.recalculateGroupScores(updated);
        return updated;
    }

    public void deleteGroupById(int id) {
        groupRepository.deleteById(id);
    }
}
