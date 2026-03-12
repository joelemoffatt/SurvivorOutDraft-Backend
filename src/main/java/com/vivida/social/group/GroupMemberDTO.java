package com.vivida.social.group;

import java.time.LocalDateTime;

/**
 * DTO for GroupMember response - breaks circular references
 */
public class GroupMemberDTO {
    public Integer id;
    public GroupMinimalDTO group;
    public UserDTO user;
    public MembershipStatus status;
    public LocalDateTime joinedAt;
    public LocalDateTime lastAccessedAt;

    public GroupMemberDTO(GroupMember member) {
        this.id = member.getId();
        this.status = member.getStatus();
        this.joinedAt = member.getJoinedAt();
        this.lastAccessedAt = member.getLastAccessedAt();
        
        if (member.getGroup() != null) {
            this.group = new GroupMinimalDTO(member.getGroup());
        }
        
        if (member.getUser() != null) {
            this.user = new UserDTO(member.getUser());
        }
    }

    public static class GroupMinimalDTO {
        public Integer id;
        public String name;

        public GroupMinimalDTO(Group group) {
            this.id = group.getId();
            this.name = group.getName();
        }
    }

    public static class UserDTO {
        public Integer id;
        public String username;
        public String email;

        public UserDTO(com.vivida.auth.User user) {
            this.id = user.getId();
            this.username = user.getUsername();
            this.email = user.getEmail();
        }
    }
}
