package com.vivida.social.group;

import org.springframework.web.bind.annotation.*;

import com.vivida.social.member.MembershipStatus;

import java.util.List;

@RestController
@RequestMapping("api/v1/group-members")
public class GroupMemberController {

    private final GroupMemberService groupMemberService;

    public GroupMemberController(GroupMemberService groupMemberService) {
        this.groupMemberService = groupMemberService;
    }

    @GetMapping
    public List<GroupMember> getGroupMembers() {
        return groupMemberService.getAllGroupMembers();
    }

    @GetMapping("{id}")
    public GroupMember getGroupMemberById(@PathVariable Integer id) {
        return groupMemberService.getGroupMemberById(id);
    }

    @GetMapping("group/{groupId}")
    public List<GroupMember> getGroupMembersByGroupId(@PathVariable Integer groupId) {
        return groupMemberService.getGroupMembersByGroupId(groupId);
    }

    @GetMapping("user/{userId}")
    public List<GroupMember> getGroupMembersByUserId(@PathVariable Integer userId) {
        return groupMemberService.getGroupMembersByUserId(userId);
    }

    @GetMapping("group/{groupId}/user/{userId}")
    public GroupMember getGroupMemberByGroupAndUser(
            @PathVariable Integer groupId,
            @PathVariable Integer userId) {
        return groupMemberService.getGroupMemberByGroupAndUser(groupId, userId);
    }

    @PostMapping
    public void inviteMember(@RequestBody GroupMember groupMember) {
        groupMemberService.inviteMember(groupMember);
    }

    @PatchMapping("{id}/status")
    public void updateMemberStatus(
            @PathVariable Integer id,
            @RequestParam MembershipStatus status) {
        groupMemberService.updateMemberStatus(id, status);
    }

    @DeleteMapping("{id}")
    public void deleteGroupMember(@PathVariable Integer id) {
        groupMemberService.deleteGroupMemberById(id);
    }
}
