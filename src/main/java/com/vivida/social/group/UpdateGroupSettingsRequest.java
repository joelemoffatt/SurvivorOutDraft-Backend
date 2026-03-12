package com.vivida.social.group;

import com.vivida.draft.DraftStyle;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateGroupSettingsRequest {
    private String name;
    private Integer seasonId;
    private Integer teamSize;
    private Integer latestWatchedEpisodeId;
    private Integer firstScoringEpisodeNumber;
    private DraftStyle style;
}
