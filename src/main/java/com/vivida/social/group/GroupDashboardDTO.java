package com.vivida.social.group;

import com.vivida.social.team.TeamDTO;

import java.util.List;

public class GroupDashboardDTO {
    public GroupDTO group;
    public List<GroupMemberDTO> members;
    public List<TeamDTO> teams;

    public GroupDashboardDTO(GroupDTO group, List<GroupMemberDTO> members, List<TeamDTO> teams) {
        this.group = group;
        this.members = members;
        this.teams = teams;
    }
}
