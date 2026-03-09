package com.vivida.social.group;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.vivida.auth.User;
import com.vivida.auth.UserRepository;
import com.vivida.social.team.Team;
import com.vivida.social.team.TeamRepository;

import java.util.List;

@Service
public class GroupMemberService {

    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final TeamRepository teamRepository;

    public GroupMemberService(GroupMemberRepository groupMemberRepository, 
                             UserRepository userRepository,
                             GroupRepository groupRepository,
                             TeamRepository teamRepository) {
        this.groupMemberRepository = groupMemberRepository;
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.teamRepository = teamRepository;
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

    public void updateMemberStatus(int id, MembershipStatus status) {
        GroupMember member = getGroupMemberById(id);
        MembershipStatus previousStatus = member.getStatus();
        member.setStatus(status);
        groupMemberRepository.save(member);
        
        // When a user accepts an invitation, create their team automatically
        if (previousStatus == MembershipStatus.INVITED && status == MembershipStatus.ACCEPTED) {
            createTeamForMember(member);
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
    
    public GroupMember inviteByUsername(Integer groupId, String username) {
        // Find the user by username
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found with username: " + username));
        
        // Find the group
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Group not found with id: " + groupId));
        
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
        
        return groupMemberRepository.save(invitation);
    }
    
    public List<GroupMember> getPendingInvitationsByUserId(Integer userId) {
        return groupMemberRepository.findByUserIdAndStatus(userId, MembershipStatus.INVITED);
    }
}
