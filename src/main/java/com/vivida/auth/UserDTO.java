package com.vivida.auth;

import java.time.LocalDateTime;

/**
 * DTO for User response - breaks circular references
 */
public class UserDTO {
    public Integer id;
    public String username;
    public String email;
    public String role;
    public Boolean enabled;
    public LocalDateTime createdAt;

    public UserDTO(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.role = user.getRole().name();
        this.enabled = user.getEnabled();
        this.createdAt = user.getCreatedAt();
    }
}
