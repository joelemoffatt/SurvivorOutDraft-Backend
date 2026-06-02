package com.vivida.social.group;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.vivida.auth.User;
import com.vivida.auth.Role;
import com.vivida.auth.UserRepository;
import com.vivida.draft.DraftStatus;
import com.vivida.notification.NotificationService;
import com.vivida.social.team.Team;
import com.vivida.social.team.TeamRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class GroupMemberService {

    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final TeamRepository teamRepository;
    private final NotificationService notificationService;

    public GroupMemberService(GroupMemberRepository groupMemberRepository,
                             UserRepository userRepository,
                             GroupRepository groupRepository,
                             TeamRepository teamRepository,
                             NotificationService notificationService) {
        this.groupMemberRepository = groupMemberRepository;
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.teamRepository = teamRepository;
        this.notificationService = notificationService;
    }

    public List<GroupMember> getAllGroupMembers() {
        return groupMemberRepository.findAll();
    }

    public GroupMember getGroupMemberById(int id) {
        return groupMemberRepository.findById(id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "GroupMember not found with id " + id
        ));
    }

    public List<GroupMember> getGroupMembersByGroupId(int groupId) {
        return groupMemberRepository.findByGroupId(groupId);
    }

    public List<GroupMember> getGroupMembersByUserId(int userId) {
        return groupMemberRepository.findByUserId(userId);
    }

    public GroupMember getGroupMemberByGroupAndUser(int groupId, int userId) {
        return groupMemberRepository.findByGroupIdAndUserId(groupId, userId).orElseThrow(() -> 
                new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "GroupMember not found for group " + groupId + " and user " + userId)
        );
    }

    public void inviteMember(GroupMember groupMember) {
        if (groupMemberRepository.existsByGroupIdAndUserId(
                groupMember.getGroup().getId(), 
                groupMember.getUser().getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User already invited to this group");
        }
        groupMemberRepository.save(groupMember);
    }

    public void updateMemberStatus(int id, MembershipStatus status, Integer requestingUserId) {
        GroupMember member = getGroupMemberById(id);
        if (!member.getUser().getId().equals(requestingUserId)) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN,
                    "You can only update your own membership status");
        }
        MembershipStatus previousStatus = member.getStatus();
        if (previousStatus == status) return;
        member.setStatus(status);
        if (status == MembershipStatus.ACCEPTED) {
            member.setLastAccessedAt(LocalDateTime.now());
        }
        groupMemberRepository.save(member);
        
        // When a user accepts an invitation, create their team and notify the group admin
        if (previousStatus == MembershipStatus.INVITED && status == MembershipStatus.ACCEPTED) {
            createTeamForMember(member);
            notificationService.deleteInviteNotification(id);
            notificationService.createInviteAccepted(member);
        }
    }
    
    private void createTeamForMember(GroupMember member) {
        // Check if team already exists
        if (teamRepository.existsByGroupIdAndUserId(member.getGroup().getId(), member.getUser().getId())) {
            return; // Team already exists
        }
        
        // Create new team
        Team team = new Team();
        team.setGroup(member.getGroup());
        team.setUser(member.getUser());
        team.setTeamName("Team " + member.getUser().getUsername());
        team.setTotalPoints(0);
        teamRepository.save(team);
    }

    public void deleteGroupMemberById(int id) {
        groupMemberRepository.deleteById(id);
    }

    public void deleteGroupMemberById(int id, Integer requestingUserId) {
        GroupMember member = getGroupMemberById(id);
        Group group = member.getGroup();

        User requestingUser = userRepository.findById(requestingUserId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Requesting user not found with id: " + requestingUserId));

        boolean isInvitee = member.getUser().getId().equals(requestingUserId);
        if (isInvitee) {
            notificationService.deleteInviteNotification(id);
            groupMemberRepository.deleteById(id);
            return;
        }

        boolean isGroupAdmin = group.getAdmin().getId().equals(requestingUserId);
        boolean isPlatformAdmin = requestingUser.getRole() == Role.ADMIN;
        if (!isGroupAdmin && !isPlatformAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Only the group admin can remove members");
        }

        ensureDraftPendingForMemberChanges(group);
        groupMemberRepository.deleteById(id);
    }
    
    public GroupMember inviteByUsername(Integer groupId, String username, Integer requestingUserId) {
        User requestingUser = userRepository.findById(requestingUserId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Requesting user not found with id: " + requestingUserId));

        // Find the user by username
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found with username: " + username));
        
        // Find the group
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Group not found with id: " + groupId));
        
        // Group admin can invite, and platform ADMIN can override
        boolean isGroupAdmin = group.getAdmin().getId().equals(requestingUserId);
        boolean isPlatformAdmin = requestingUser.getRole() == Role.ADMIN;
        if (!isGroupAdmin && !isPlatformAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Only the group admin can invite members");
        }

        ensureDraftPendingForMemberChanges(group);
        
        // Check if user is already a member or invited
        if (groupMemberRepository.existsByGroupIdAndUserId(groupId, user.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, 
                    "User is already a member or has been invited to this group");
        }
        
        // Create and save the invitation
        GroupMember invitation = new GroupMember();
        invitation.setGroup(group);
        invitation.setUser(user);
        invitation.setStatus(MembershipStatus.INVITED);
        
        GroupMember saved = groupMemberRepository.save(invitation);
        notificationService.createInviteReceived(saved, requestingUser);
        return saved;
    }

    private void ensureDraftPendingForMemberChanges(Group group) {
        if (group == null || group.getDraft() == null || group.getDraft().getStatus() != DraftStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Members can only be modified while the draft is pending");
        }
    }
    
    public List<GroupMember> getPendingInvitationsByUserId(Integer userId) {
        return groupMemberRepository.findByUserIdAndStatus(userId, MembershipStatus.INVITED);
    }

    @Transactional
    public GroupMember markGroupAccessed(Integer groupId, Integer userId) {
        GroupMember member = groupMemberRepository.findByGroupIdAndUserIdAndStatus(
                        groupId,
                        userId,
                        MembershipStatus.ACCEPTED)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "You must be an accepted member of this group"));

        member.setLastAccessedAt(LocalDateTime.now());
        return groupMemberRepository.save(member);
    }
}
