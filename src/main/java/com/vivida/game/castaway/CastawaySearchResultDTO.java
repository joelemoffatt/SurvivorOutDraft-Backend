package com.vivida.game.castaway;

public record CastawaySearchResultDTO(
        Integer castawayId,
        Integer season,
        String fullName,
        String jsonId
) {
}
