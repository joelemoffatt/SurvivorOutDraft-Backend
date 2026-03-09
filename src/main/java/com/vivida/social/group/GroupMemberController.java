package com.vivida.social.group;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/group-members")
public class GroupMemberController {

    private final GroupMemberService groupMemberService;

    public GroupMemberController(GroupMemberService groupMemberService) {
        this.groupMemberService = groupMemberService;
    }
    
    @PostMapping("invite-by-username")
    public GroupMemberDTO inviteByUsername(@RequestBody InviteByUsernameRequest request) {
        GroupMember member = groupMemberService.inviteByUsername(request.getGroupId(), request.getUsername());
        return new GroupMemberDTO(member);
    }
    
    @GetMapping("user/{userId}/pending")
    public List<GroupMemberDTO> getPendingInvitations(@PathVariable Integer userId) {
        return groupMemberService.getPendingInvitationsByUserId(userId).stream()
                .map(GroupMemberDTO::new)
                .toList();
    }

    @GetMapping
    public List<GroupMemberDTO> getGroupMembers() {
        return groupMemberService.getAllGroupMembers().stream()
                .map(GroupMemberDTO::new)
                .toList();
    }

    @GetMapping("{id}")
    public GroupMemberDTO getGroupMemberById(@PathVariable Integer id) {
        return new GroupMemberDTO(groupMemberService.getGroupMemberById(id));
    }

    @GetMapping("group/{groupId}")
    public List<GroupMemberDTO> getGroupMembersByGroupId(@PathVariable Integer groupId) {
        return groupMemberService.getGroupMembersByGroupId(groupId).stream()
                .map(GroupMemberDTO::new)
                .toList();
    }

    @GetMapping("user/{userId}")
    public List<GroupMemberDTO> getGroupMembersByUserId(@PathVariable Integer userId) {
        return groupMemberService.getGroupMembersByUserId(userId).stream()
                .map(GroupMemberDTO::new)
                .toList();
    }

    @GetMapping("group/{groupId}/user/{userId}")
    public GroupMemberDTO getGroupMemberByGroupAndUser(
            @PathVariable Integer groupId,
            @PathVariable Integer userId) {
        return new GroupMemberDTO(groupMemberService.getGroupMemberByGroupAndUser(groupId, userId));
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

