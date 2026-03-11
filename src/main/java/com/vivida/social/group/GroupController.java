package com.vivida.social.group;

import com.vivida.draft.DraftService;
import com.vivida.draft.DraftStyle;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.vivida.auth.User;
import com.vivida.auth.UserRepository;
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
    private final SeasonRepository seasonRepository;

    public GroupController(GroupService groupService,
                           DraftService draftService,
                           UserRepository userRepository,
                           SeasonRepository seasonRepository) {
        this.groupService = groupService;
        this.draftService = draftService;
        this.userRepository = userRepository;
        this.seasonRepository = seasonRepository;
    }

    @GetMapping
    public List<GroupDTO> getGroups() {
        return groupService.getAllGroups().stream()
                .map(GroupDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @GetMapping("{id}")
    public GroupDTO getGroupById(@PathVariable Integer id) {
        return GroupDTO.fromEntity(groupService.getGroupById(id));
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
        if (request.getLatestEpisodeWatched() == null || request.getLatestEpisodeWatched() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Latest episode watched is required and must be 0 or greater");
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

        // Create Group entity
        Group group = new Group();
        group.setName(request.getName().trim());
        group.setAdmin(admin);
        group.setSeason(season);
        group.setTeamSize(request.getTeamSize());
        group.setLatestEpisodeWatched(request.getLatestEpisodeWatched());
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

        return GroupDTO.fromEntity(groupService.getGroupById(savedGroup.getId()));
    }

    @PutMapping
    public void updateGroup(@RequestBody Group group) {
        groupService.updateGroup(group);
    }

    @PatchMapping("{id}/watched-episode")
    public GroupDTO updateLatestWatchedEpisode(@PathVariable Integer id,
                                               @RequestBody UpdateWatchedEpisodeRequest request) {
        if (request == null || request.getLatestEpisodeWatched() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "latestEpisodeWatched is required");
        }
        Group updated = groupService.updateLatestEpisodeWatched(id, request.getLatestEpisodeWatched());
        return GroupDTO.fromEntity(updated);
    }

    @DeleteMapping("{id}")
    public void deleteGroup(@PathVariable Integer id) {
        groupService.deleteGroupById(id);
    }
}
