package com.vivida.dto;

import java.util.List;

public record TribePerformanceGroupDto(
        String tribeName,
        List<ChallengePerformanceRowDto> performances
) {
}
