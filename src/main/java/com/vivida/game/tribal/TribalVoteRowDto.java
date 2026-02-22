package com.vivida.game.tribal;

public record TribalVoteRowDto(
        String voterName,
        String votedForName,
        Boolean nullified
) {
}
