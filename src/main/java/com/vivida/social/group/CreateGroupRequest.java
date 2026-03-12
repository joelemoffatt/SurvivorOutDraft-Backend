package com.vivida.social.group;

import com.vivida.draft.DraftStyle;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateGroupRequest {
    private String name;
    private AdminRef admin;
    private SeasonRef season;
    private Integer teamSize;
    private EpisodeRef latestWatchedEpisode;
    private Integer firstScoringEpisodeNumber;
    private DraftStyle style;
    private LocalDateTime scheduledAt;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AdminRef {
        private Integer id;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SeasonRef {
        private Integer id;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class EpisodeRef {
        private Integer id;
    }
}
