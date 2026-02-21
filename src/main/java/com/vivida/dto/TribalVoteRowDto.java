package com.vivida.dto;

public record TribalVoteRowDto(
        String voterName,
        String votedForName,
        Boolean nullified
) {
}
