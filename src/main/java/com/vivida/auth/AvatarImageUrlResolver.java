package com.vivida.auth;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

public final class AvatarImageUrlResolver {

    private AvatarImageUrlResolver() {
    }

    public static String buildAvatarUrl(Integer userId) {
        if (userId == null) {
            return null;
        }

        try {
            return ServletUriComponentsBuilder
                    .fromCurrentContextPath()
                    .path("/api/v1/users/{id}/avatar")
                    .buildAndExpand(userId)
                    .toUriString();
        } catch (IllegalStateException ex) {
            return "/api/v1/users/" + userId + "/avatar";
        }
    }
}