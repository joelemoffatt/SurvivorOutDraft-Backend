package com.vivida.dto;

import java.util.List;

public record ChallengeDetailDto(
        String title,
        String type,
        Integer number,
        List<TribePerformanceGroupDto> performancesByTribe
) {
}
