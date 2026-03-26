package com.vivida.social.team;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

public final class TeamAvatarImageUrlResolver {

    private TeamAvatarImageUrlResolver() {
    }

    public static String buildAvatarUrl(Integer teamId) {
        if (teamId == null) {
            return null;
        }

        try {
            return ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/v1/teams/{id}/avatar")
                    .buildAndExpand(teamId)
                    .toUriString();
        } catch (IllegalStateException ex) {
            return "/api/v1/teams/" + teamId + "/avatar";
        }
    }
}
