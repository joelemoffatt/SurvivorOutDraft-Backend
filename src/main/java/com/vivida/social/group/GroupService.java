package com.vivida.social.group;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.vivida.social.member.MembershipStatus;

import java.util.List;

@Service
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;

    public GroupService(GroupRepository groupRepository, GroupMemberRepository groupMemberRepository) {
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
    }

    public List<Group> getAllGroups() {
        return groupRepository.findAll();
    }

    public Group getGroupById(int id) {
        return groupRepository.findById(id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Group not found with id " + id
        ));
    }

    public List<Group> getGroupsByAdminId(int adminId) {
        return groupRepository.findByAdminId(adminId);
    }

    public List<Group> getGroupsBySeasonId(int seasonId) {
        return groupRepository.findBySeasonSeason(seasonId);
    }

    public List<Group> getGroupsByUserId(int userId) {
        List<GroupMember> memberships = groupMemberRepository.findByUserIdAndStatus(userId, MembershipStatus.ACCEPTED);
        return memberships.stream().map(GroupMember::getGroup).toList();
    }

    public void insertGroup(Group group) {
        groupRepository.save(group);
    }

    public void updateGroup(Group group) {
        groupRepository.save(group);
    }

    public void deleteGroupById(int id) {
        groupRepository.deleteById(id);
    }
}
