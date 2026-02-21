package com.vivida.dto;

public record JourneyDetailDto(
        String castawayName,
        String event,
        String reward,
        Boolean lostVote,
        Boolean choseToPlay
) {
}
