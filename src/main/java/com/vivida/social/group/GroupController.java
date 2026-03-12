package com.vivida.social.group;

import com.vivida.draft.DraftService;
import com.vivida.draft.DraftStyle;
import com.vivida.scoring.PointRule;
import com.vivida.scoring.PointRuleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.vivida.auth.User;
import com.vivida.auth.UserRepository;
import com.vivida.game.episode.Episode;
import com.vivida.game.episode.EpisodeRepository;
import com.vivida.game.season.Season;
import com.vivida.game.season.SeasonRepository;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/v1/groups")
public class GroupController {

    private final GroupService groupService;
    private final DraftService draftService;
    private final UserRepository userRepository;
    private final EpisodeRepository episodeRepository;
    private final SeasonRepository seasonRepository;
    private final PointRuleRepository pointRuleRepository;

    public GroupController(GroupService groupService,
                           DraftService draftService,
                           UserRepository userRepository,
                           EpisodeRepository episodeRepository,
                           SeasonRepository seasonRepository,
                           PointRuleRepository pointRuleRepository) {
        this.groupService = groupService;
        this.draftService = draftService;
        this.userRepository = userRepository;
        this.episodeRepository = episodeRepository;
        this.seasonRepository = seasonRepository;
        this.pointRuleRepository = pointRuleRepository;
    }

    @GetMapping
    public List<GroupDTO> getGroups() {
        return groupService.getAllGroups().stream()
                .map(GroupDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @GetMapping("{id}")
    public GroupDTO getGroupById(@PathVariable Integer id) {
        Group group = groupService.getGroupById(id);
        return GroupDTO.fromEntity(group, pointRuleRepository.findByGroupId(id));
    }

    @GetMapping("admin/{adminId}")
    public List<GroupDTO> getGroupsByAdminId(@PathVariable Integer adminId) {
        return groupService.getGroupsByAdminId(adminId).stream()
                .map(GroupDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @GetMapping("season/{seasonId}")
    public List<GroupDTO> getGroupsBySeasonId(@PathVariable Integer seasonId) {
        return groupService.getGroupsBySeasonId(seasonId).stream()
                .map(GroupDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @GetMapping("user/{userId}")
    public List<GroupDTO> getGroupsByUserId(@PathVariable Integer userId) {
        return groupService.getGroupsByUserId(userId).stream()
                .map(GroupDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @PostMapping
    public GroupDTO addGroup(@RequestBody CreateGroupRequest request) {
        // Validate required fields
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Group name is required");
        }
        if (request.getAdmin() == null || request.getAdmin().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Admin user is required");
        }
        if (request.getSeason() == null || request.getSeason().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Season is required");
        }
        if (request.getTeamSize() == null || request.getTeamSize() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Team size must be a positive integer");
        }
        if (request.getPointRules() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "pointRules list is required");
        }

        // Fetch User (admin) entity
        User admin = userRepository.findById(request.getAdmin().getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Admin user not found with id " + request.getAdmin().getId()
                ));

        // Fetch Season entity
        Season season = seasonRepository.findById(request.getSeason().getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Season not found with id " + request.getSeason().getId()
                ));

        // Resolve optional latest watched episode
        Episode latestWatchedEpisode = null;
        if (request.getLatestWatchedEpisode() != null && request.getLatestWatchedEpisode().getId() != null) {
            latestWatchedEpisode = episodeRepository.findById(request.getLatestWatchedEpisode().getId())
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Episode not found with id " + request.getLatestWatchedEpisode().getId()
                ));
            if (latestWatchedEpisode.getSeason() == null
                || !latestWatchedEpisode.getSeason().getSeason().equals(season.getSeason())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Latest watched episode must belong to the selected season");
            }
        }

        // Create Group entity
        Group group = new Group();
        group.setName(request.getName().trim());
        group.setAdmin(admin);
        group.setSeason(season);
        group.setTeamSize(request.getTeamSize());
        group.setLatestEpisodeWatched(latestWatchedEpisode); // null = no episodes watched
        group.setFirstScoringEpisodeNumber(
            request.getFirstScoringEpisodeNumber() != null && request.getFirstScoringEpisodeNumber() > 0
                ? request.getFirstScoringEpisodeNumber()
                : 1
        );
        group.setStatus(GroupStatus.PENDING);

        // Save group and automatically add admin as member
        Group savedGroup = groupService.createGroupWithAdmin(group);
        
        // If a draft style is provided, create a pending draft for this group
        draftService.createPendingDraftForGroup(
            savedGroup,
            admin,
            request.getStyle() != null ? request.getStyle() : DraftStyle.SNAKE,
            request.getTeamSize(),
            request.getScheduledAt()
        );

        // Create point rules from request list
        List<PointRule> rules = groupService.createRulesForGroup(savedGroup, request.getPointRules());

        return GroupDTO.fromEntity(savedGroup, rules);
    }

    @PutMapping
    public void updateGroup(@RequestBody Group group) {
        groupService.updateGroup(group);
    }

    @PatchMapping("{id}/settings")
    public GroupDTO updateGroupSettings(@PathVariable Integer id,
                                        @RequestBody UpdateGroupSettingsRequest request,
                                        Authentication authentication) {
        User requestingUser = (User) authentication.getPrincipal();
        Group group = groupService.getGroupById(id);
        if (!group.getAdmin().getId().equals(requestingUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Only the group admin can update group settings");
        }
        Group updated = groupService.updateGroupSettings(id, request);
        return GroupDTO.fromEntity(updated, pointRuleRepository.findByGroupId(id));
    }

    @PatchMapping("{id}/watched-episode")
    public GroupDTO updateLatestWatchedEpisode(@PathVariable Integer id,
                                               @RequestBody UpdateWatchedEpisodeRequest request,
                                               Authentication authentication) {
        User requestingUser = (User) authentication.getPrincipal();
        if (request == null || request.getEpisodeId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "episodeId is required");
        }
        Group group = groupService.getGroupById(id);
        if (!group.getAdmin().getId().equals(requestingUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Only the group admin can update the watched episode");
        }
        Group updated = groupService.updateLatestEpisodeWatched(id, request.getEpisodeId());
        return GroupDTO.fromEntity(updated);
    }

    @PatchMapping("{id}/first-scoring-episode")
    public GroupDTO updateFirstScoringEpisode(@PathVariable Integer id,
                                              @RequestBody UpdateFirstScoringEpisodeRequest request,
                                              Authentication authentication) {
        User requestingUser = (User) authentication.getPrincipal();
        if (request == null || request.getFirstScoringEpisodeNumber() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "firstScoringEpisodeNumber is required");
        }
        Group group = groupService.getGroupById(id);
        if (!group.getAdmin().getId().equals(requestingUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Only the group admin can update the first scoring episode");
        }
        Group updated = groupService.updateFirstScoringEpisodeNumber(id, request.getFirstScoringEpisodeNumber());
        return GroupDTO.fromEntity(updated);
    }

    @DeleteMapping("{id}")
    public void deleteGroup(@PathVariable Integer id) {
        groupService.deleteGroupById(id);
    }
}
