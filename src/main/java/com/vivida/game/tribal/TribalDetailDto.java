package com.vivida.game.tribal;

import java.util.List;

public record TribalDetailDto(
        String tribeName,
        Integer bootOrder,
        String votedOutName,
        List<TribalVoteRowDto> votes
) {
}
