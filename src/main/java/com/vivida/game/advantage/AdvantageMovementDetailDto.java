package com.vivida.game.advantage;

public record AdvantageMovementDetailDto(
        String castawayName,
        String playedForName,
        String advantageType,
        String event,
        String success,
        Integer votesNullified
) {
}
