package com.vivida.social.group;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import com.vivida.game.episode.EpisodeRepository;
import com.vivida.social.team.Team;
import com.vivida.social.team.TeamRepository;

import java.util.List;

@Service
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final TeamRepository teamRepository;
    private final EpisodeRepository episodeRepository;

    public GroupService(GroupRepository groupRepository,
                        GroupMemberRepository groupMemberRepository,
                        TeamRepository teamRepository,
                        EpisodeRepository episodeRepository) {
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.teamRepository = teamRepository;
        this.episodeRepository = episodeRepository;
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

    @Transactional
    public Group createGroupWithAdmin(Group group) {
        // Save the group first
        Group savedGroup = groupRepository.save(group);
        
        // Automatically add admin as an ACCEPTED member
        GroupMember adminMembership = new GroupMember();
        adminMembership.setGroup(savedGroup);
        adminMembership.setUser(savedGroup.getAdmin());
        adminMembership.setStatus(MembershipStatus.ACCEPTED);
        groupMemberRepository.save(adminMembership);
        
        // Automatically create a team for the admin
        Team adminTeam = new Team();
        adminTeam.setGroup(savedGroup);
        adminTeam.setUser(savedGroup.getAdmin());
        adminTeam.setTeamName("Team " + savedGroup.getAdmin().getUsername());
        adminTeam.setTotalPoints(0);
        teamRepository.save(adminTeam);
        
        return savedGroup;
    }

    public void updateGroup(Group group) {
        groupRepository.save(group);
    }

    @Transactional
    public Group updateLatestEpisodeWatched(Integer groupId, Integer episodeId) {
        if (episodeId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "episodeId is required");
        }

        Group group = getGroupById(groupId);

        com.vivida.game.episode.Episode episode = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Episode not found with id " + episodeId));

        if (episode.getSeason() == null
                || !episode.getSeason().getSeason().equals(group.getSeason().getSeason())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Episode must belong to the group's season");
        }

        group.setLatestEpisodeWatched(episode);
        return groupRepository.save(group);
    }

    public void deleteGroupById(int id) {
        groupRepository.deleteById(id);
    }
}
