package com.vivida.game.challenge;

import java.util.List;

import com.vivida.game.tribe.TribePerformanceGroupDto;

public record ChallengeDetailDto(
        String title,
        String type,
        Integer number,
        List<TribePerformanceGroupDto> performancesByTribe
) {
}
