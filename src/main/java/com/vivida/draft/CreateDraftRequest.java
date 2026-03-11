package com.vivida.draft;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CreateDraftRequest {

    private Integer groupId;

    /** SNAKE, ROUND_ROBIN, or LINEAR. Defaults to SNAKE if omitted. */
    private DraftStyle style;

    private Integer teamSize;

    /** Optional — schedule the draft for a future date/time. */
    private LocalDateTime scheduledAt;
}
