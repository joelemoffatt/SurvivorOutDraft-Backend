package com.vivida.game.tribe;

import java.util.List;

import com.vivida.game.challenge.ChallengePerformanceRowDto;

public record TribePerformanceGroupDto(
        String tribeName,
        List<ChallengePerformanceRowDto> performances
) {
}
