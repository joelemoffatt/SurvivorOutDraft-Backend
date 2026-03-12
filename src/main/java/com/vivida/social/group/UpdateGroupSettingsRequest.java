package com.vivida.social.group;

import com.vivida.draft.DraftStyle;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

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
    private List<PointRuleRequest> pointRules = new ArrayList<>();

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PointRuleRequest {
        private String ruleType;
        private Integer points;
    }
}
