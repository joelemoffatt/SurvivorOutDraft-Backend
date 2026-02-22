package com.vivida.game.journey;

public record JourneyDetailDto(
        String castawayName,
        String event,
        String reward,
        Boolean lostVote,
        Boolean choseToPlay
) {
}
