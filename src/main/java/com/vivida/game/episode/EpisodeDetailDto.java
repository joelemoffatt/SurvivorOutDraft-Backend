package com.vivida.game.episode;

import java.util.List;

import com.vivida.game.advantage.AdvantageMovementDetailDto;
import com.vivida.game.boot.BootDetailDto;
import com.vivida.game.challenge.ChallengeDetailDto;
import com.vivida.game.journey.JourneyDetailDto;
import com.vivida.game.juryVote.JuryVoteDetailDto;
import com.vivida.game.tribal.TribalDetailDto;

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
