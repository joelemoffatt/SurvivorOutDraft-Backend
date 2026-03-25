package com.vivida.auth;

import com.vivida.game.castaway.Castaway;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for User response - breaks circular references
 */
public class UserDTO {
    public Integer id;
    public String username;
    public String email;
    public String role;
    public Boolean enabled;
    public String avatarImage;
    public List<Castaway> favoriteCastaways;
    public String bio;
    public LocalDateTime createdAt;

    public UserDTO(User user) {
        this(user, false);
    }

    public UserDTO(User user, boolean hasAvatar) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.role = user.getRole().name();
        this.enabled = user.getEnabled();
        this.avatarImage = hasAvatar ? AvatarImageUrlResolver.buildAvatarUrl(user.getId()) : null;
        this.favoriteCastaways = user.getFavoriteCastaways();
        this.bio = user.getBio();
        this.createdAt = user.getCreatedAt();
    }
}
