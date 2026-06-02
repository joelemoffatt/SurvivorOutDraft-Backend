package com.vivida.social.group;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import com.vivida.draft.DraftStatus;
import com.vivida.draft.DraftRepository;
import com.vivida.draft.DraftStyle;
import com.vivida.game.boot.BootRepository;
import com.vivida.game.castaway.CastawayPerformanceRepository;
import com.vivida.game.episode.Episode;
import com.vivida.game.episode.EpisodeRepository;
import com.vivida.game.season.Season;
import com.vivida.game.season.SeasonRepository;
import com.vivida.scoring.PointRule;
import com.vivida.scoring.PointRuleRepository;
import com.vivida.scoring.RuleType;
import com.vivida.notification.NotificationService;
import com.vivida.scoring.ScoreProjectionService;
import com.vivida.scoring.TeamCastawayScoreEventRepository;
import com.vivida.social.team.Team;
import com.vivida.social.team.TeamRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final TeamRepository teamRepository;
    private final CastawayPerformanceRepository castawayPerformanceRepository;
    private final BootRepository bootRepository;
    private final EpisodeRepository episodeRepository;
    private final SeasonRepository seasonRepository;
    private final DraftRepository draftRepository;
    private final ScoreProjectionService scoreProjectionService;
    private final PointRuleRepository pointRuleRepository;
    private final TeamCastawayScoreEventRepository teamCastawayScoreEventRepository;
    private final NotificationService notificationService;

    public GroupService(GroupRepository groupRepository,
            GroupMemberRepository groupMemberRepository,
            TeamRepository teamRepository,
            CastawayPerformanceRepository castawayPerformanceRepository,
            BootRepository bootRepository,
            EpisodeRepository episodeRepository,
            SeasonRepository seasonRepository,
            DraftRepository draftRepository,
            ScoreProjectionService scoreProjectionService,
            PointRuleRepository pointRuleRepository,
            TeamCastawayScoreEventRepository teamCastawayScoreEventRepository,
            NotificationService notificationService) {
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.teamRepository = teamRepository;
        this.castawayPerformanceRepository = castawayPerformanceRepository;
        this.bootRepository = bootRepository;
        this.episodeRepository = episodeRepository;
        this.seasonRepository = seasonRepository;
        this.draftRepository = draftRepository;
        this.scoreProjectionService = scoreProjectionService;
        this.pointRuleRepository = pointRuleRepository;
        this.teamCastawayScoreEventRepository = teamCastawayScoreEventRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public List<PointRule> createRulesForGroup(Group group,
            List<CreateGroupRequest.PointRuleRequest> ruleRequests) {
        if (group == null || group.getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Group is required");
        }
        if (ruleRequests == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "pointRules list is required");
        }
        List<PointRule> rules = new ArrayList<>();
        for (CreateGroupRequest.PointRuleRequest req : ruleRequests) {
            if (req == null || req.getRuleType() == null || req.getPoints() == null) {
                continue;
            }
            PointRule rule = new PointRule();
            rule.setGroup(group);
            rule.setRuleType(parseRuleType(req.getRuleType()));
            rule.setPoints(req.getPoints());
            rules.add(rule);
        }
        return pointRuleRepository.saveAll(rules);
    }

    private RuleType parseRuleType(String ruleTypeRaw) {
        try {
            return RuleType.valueOf(ruleTypeRaw);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid ruleType: " + ruleTypeRaw);
        }
    }

    @Transactional
    public List<PointRule> syncRulesForGroup(Integer groupId,
            List<UpdateGroupSettingsRequest.PointRuleRequest> ruleRequests) {
        Group group = getGroupById(groupId);
        if (ruleRequests == null) {
            return pointRuleRepository.findByGroupId(groupId);
        }

        Map<RuleType, Integer> requested = new LinkedHashMap<>();
        for (UpdateGroupSettingsRequest.PointRuleRequest req : ruleRequests) {
            if (req == null || req.getRuleType() == null || req.getPoints() == null) {
                continue;
            }
            requested.put(parseRuleType(req.getRuleType()), req.getPoints());
        }

        List<PointRule> existing = pointRuleRepository.findByGroupId(groupId);
        Map<RuleType, PointRule> existingByType = new LinkedHashMap<>();
        List<PointRule> duplicates = new ArrayList<>();
        for (PointRule rule : existing) {
            if (!existingByType.containsKey(rule.getRuleType())) {
                existingByType.put(rule.getRuleType(), rule);
            } else {
                duplicates.add(rule);
            }
        }

        List<PointRule> toSave = new ArrayList<>();
        for (Map.Entry<RuleType, Integer> entry : requested.entrySet()) {
            RuleType ruleType = entry.getKey();
            Integer points = entry.getValue();
            PointRule existingRule = existingByType.get(ruleType);
            if (existingRule == null) {
                PointRule created = new PointRule();
                created.setGroup(group);
                created.setRuleType(ruleType);
                created.setPoints(points);
                toSave.add(created);
            } else if (!points.equals(existingRule.getPoints())) {
                existingRule.setPoints(points);
                toSave.add(existingRule);
            }
        }

        if (!toSave.isEmpty()) {
            pointRuleRepository.saveAll(toSave);
        }

        List<PointRule> toDelete = new ArrayList<>(duplicates);
        for (PointRule existingRule : existingByType.values()) {
            if (!requested.containsKey(existingRule.getRuleType())) {
                toDelete.add(existingRule);
            }
        }

        if (!toDelete.isEmpty()) {
            teamCastawayScoreEventRepository.deleteByGroupId(groupId);
            pointRuleRepository.deleteAll(toDelete);
        }

        return pointRuleRepository.findByGroupId(groupId);
    }

    public List<Group> getAllGroups() {
        return groupRepository.findAll();
    }

    public Group getGroupById(int id) {
        return groupRepository.findById(id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Group not found with id " + id));
    }

    public List<Group> getGroupsByAdminId(int adminId) {
        return groupRepository.findByAdminId(adminId);
    }

    public List<Group> getGroupsBySeasonId(int seasonId) {
        return groupRepository.findBySeasonSeason(seasonId);
    }

    public List<Group> getGroupsByUserId(int userId) {
        List<GroupMember> memberships = groupMemberRepository.findByUserIdAndStatusOrderByRecentAccess(
                userId,
                MembershipStatus.ACCEPTED);
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
        if (group == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Group is required");
        }
        Integer resolvedTeamSize = group.getDraft() != null ? group.getDraft().getTeamSize() : null;
        if (resolvedTeamSize == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Draft teamSize is required");
        }
        Integer seasonId = group != null && group.getSeason() != null ? group.getSeason().getSeason() : null;
        Integer latestWatchedEpisodeNumber = group != null && group.getLatestEpisodeWatched() != null
                ? group.getLatestEpisodeWatched().getEpisodeNumber()
                : null;
        validateTeamSizeWithinAvailableCastaways(resolvedTeamSize, seasonId, latestWatchedEpisodeNumber);
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
        applyStatusFromLatestWatchedEpisode(group, episode);
        Group updated = groupRepository.save(group);
        scoreProjectionService.recalculateGroupScores(updated);
        notificationService.createEpisodeScored(updated, episode);
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
        Integer requestedTeamSize = request.getDraft() != null ? request.getDraft().getTeamSize() : null;
        if (requestedTeamSize == null || requestedTeamSize <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Team size must be a positive integer");
        }

        Integer firstScoringEpisodeNumber = request.getFirstScoringEpisodeNumber() != null
                ? request.getFirstScoringEpisodeNumber()
                : 1;
        DraftStyle style = request.getDraft() != null && request.getDraft().getStyle() != null
                ? request.getDraft().getStyle()
                : DraftStyle.SNAKE;
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

        Integer latestWatchedEpisodeNumber = latestWatchedEpisode != null
                ? latestWatchedEpisode.getEpisodeNumber()
                : null;
        if (group.getStatus() == GroupStatus.PENDING || group.getStatus() == GroupStatus.DRAFTING) {
            validateTeamSizeWithinAvailableCastaways(
            requestedTeamSize,
            season.getSeason(),
            latestWatchedEpisodeNumber);
        }

        group.setName(request.getName().trim());
        group.setSeason(season);
        group.setLatestEpisodeWatched(latestWatchedEpisode);
        applyStatusFromLatestWatchedEpisode(group, latestWatchedEpisode);
        group.setFirstScoringEpisodeNumber(firstScoringEpisodeNumber);

        Group updated = groupRepository.save(group);

        // Sync point rules (create/update/delete by ruleType) when provided.
        if (request.getPointRules() != null) {
            syncRulesForGroup(groupId, request.getPointRules());
        }

        // Keep pending draft configuration in sync with editable group settings.
        draftRepository.findByGroupIdAndStatus(groupId, DraftStatus.PENDING).ifPresent(draft -> {
            draft.setSeason(season);
            draft.setTeamSize(requestedTeamSize);
            draft.setStyle(style);
            if (request.getDraft() != null) {
                draft.setScheduledAt(request.getDraft().getScheduledAt());
            }
            draftRepository.save(draft);
        });

        scoreProjectionService.recalculateGroupScores(updated);
        return updated;
    }

    public void validateTeamSizeWithinAvailableCastaways(Integer teamSize,
            Integer seasonId,
            Integer latestWatchedEpisodeNumber) {
        if (teamSize == null || teamSize <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Team size must be a positive integer");
        }
        if (seasonId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Season is required");
        }

        int totalCastaways = (int) castawayPerformanceRepository.countBySeasonId(seasonId);
        int bootedCastaways = latestWatchedEpisodeNumber == null
                ? 0
                : bootRepository.findBySeasonIdAndEpisodeNumberLessThanEqual(seasonId, latestWatchedEpisodeNumber)
                        .size();
        int availableCastaways = Math.max(totalCastaways - bootedCastaways, 0);

        if (teamSize > availableCastaways) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Team size cannot exceed available castaways (" + availableCastaways + ")");
        }
    }

    private void applyStatusFromLatestWatchedEpisode(Group group, Episode latestWatchedEpisode) {
        if (group == null || latestWatchedEpisode == null) {
            return;
        }

        // Do not transition group lifecycle before the draft is completed.
        if (group.getDraft() == null || group.getDraft().getStatus() != DraftStatus.COMPLETED) {
            return;
        }

        if (Boolean.TRUE.equals(latestWatchedEpisode.getIsFinale())) {
            group.setStatus(GroupStatus.COMPLETED);
        } else {
            group.setStatus(GroupStatus.ACTIVE);
        }
    }

    @Transactional
    public Group markGroupAccessed(Integer groupId, Integer userId) {
        Group group = getGroupById(groupId);
        GroupMember membership = groupMemberRepository.findByGroupIdAndUserIdAndStatus(
                groupId,
                userId,
                MembershipStatus.ACCEPTED)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "You must be an accepted member of this group"));
        membership.setLastAccessedAt(java.time.LocalDateTime.now());
        groupMemberRepository.save(membership);
        return group;
    }

    public List<PointRule> getRulesForGroup(Integer groupId) {
        return pointRuleRepository.findByGroupId(groupId);
    }

    public void deleteGroupById(int id) {
        groupRepository.deleteById(id);
    }
}
