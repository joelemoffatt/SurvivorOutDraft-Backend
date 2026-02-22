package com.vivida.social.group;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.vivida.social.member.MembershipStatus;

import java.util.List;

@Service
public class GroupMemberService {

    private final GroupMemberRepository groupMemberRepository;

    public GroupMemberService(GroupMemberRepository groupMemberRepository) {
        this.groupMemberRepository = groupMemberRepository;
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
        member.setStatus(status);
        groupMemberRepository.save(member);
    }

    public void deleteGroupMemberById(int id) {
        groupMemberRepository.deleteById(id);
    }
}
