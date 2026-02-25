package com.vivida.social.group;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/v1/groups")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
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
    public void addGroup(@RequestBody Group group) {
        groupService.insertGroup(group);
    }

    @PutMapping
    public void updateGroup(@RequestBody Group group) {
        groupService.updateGroup(group);
    }

    @DeleteMapping("{id}")
    public void deleteGroup(@PathVariable Integer id) {
        groupService.deleteGroupById(id);
    }
}
