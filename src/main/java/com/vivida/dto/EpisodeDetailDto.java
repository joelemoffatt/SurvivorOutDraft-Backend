package com.vivida.dto;

import java.util.List;

public record EpisodeDetailDto(
        String episodeTitle,
        Integer episodeNumber,
        List<ChallengeDetailDto> challenges,
        List<JourneyDetailDto> journeys,
        List<AdvantageMovementDetailDto> advantageMovements,
        List<TribalDetailDto> tribals,
        List<BootDetailDto> boots,
        List<BootDetailDto> finalResultsBoots,
        List<JuryVoteDetailDto> juryVotes,
        List<FinalRankingDetailDto> finalThreeRanking
) {
}
