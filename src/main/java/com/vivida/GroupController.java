package com.vivida;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/groups")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @GetMapping
    public List<Group> getGroups() {
        return groupService.getAllGroups();
    }

    @GetMapping("{id}")
    public Group getGroupById(@PathVariable Integer id) {
        return groupService.getGroupById(id);
    }

    @GetMapping("admin/{adminId}")
    public List<Group> getGroupsByAdminId(@PathVariable Integer adminId) {
        return groupService.getGroupsByAdminId(adminId);
    }

    @GetMapping("season/{seasonId}")
    public List<Group> getGroupsBySeasonId(@PathVariable Integer seasonId) {
        return groupService.getGroupsBySeasonId(seasonId);
    }

    @GetMapping("user/{userId}")
    public List<Group> getGroupsByUserId(@PathVariable Integer userId) {
        return groupService.getGroupsByUserId(userId);
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
