package com.vivida.social.group;

import com.vivida.draft.Draft;
import com.vivida.draft.DraftStatus;
import com.vivida.draft.DraftStyle;
import com.vivida.scoring.PointRule;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GroupDTO {
    private Integer id;
    private String name;
    private AdminDTO admin;
    private SeasonDTO season;
    private DraftRefDTO draft;
    private Integer firstScoringEpisodeNumber;
    private EpisodeDTO latestEpisodeWatched;
    private GroupStatus status;
    private LocalDateTime createdAt;
    private List<PointRuleDTO> pointRules;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AdminDTO {
        private Integer id;
        private String username;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SeasonDTO {
        private Integer id;
        private String seasonName;
        private String version;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class EpisodeDTO {
        private Integer id;
        private Integer episodeNumber;
        private String episodeTitle;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PointRuleDTO {
        private String ruleType;
        private Integer points;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DraftRefDTO {
        private Integer id;
        private DraftStatus status;
        private DraftStyle style;
        private Integer teamSize;
        private LocalDateTime scheduledAt;
        private LocalDateTime startedAt;
        private LocalDateTime completedAt;
    }

    public static GroupDTO fromEntity(Group group) {
        GroupDTO dto = new GroupDTO();
        dto.setId(group.getId());
        dto.setName(group.getName());
        dto.setFirstScoringEpisodeNumber(group.getFirstScoringEpisodeNumber());
        if (group.getLatestEpisodeWatched() != null) {
            EpisodeDTO epDTO = new EpisodeDTO();
            epDTO.setId(group.getLatestEpisodeWatched().getId());
            epDTO.setEpisodeNumber(group.getLatestEpisodeWatched().getEpisodeNumber());
            epDTO.setEpisodeTitle(group.getLatestEpisodeWatched().getEpisodeTitle());
            dto.setLatestEpisodeWatched(epDTO);
        }
        dto.setStatus(group.getStatus());
        dto.setCreatedAt(group.getCreatedAt());

        Draft draft = group.getDraft();
        if (draft != null) {
            DraftRefDTO draftDTO = new DraftRefDTO();
            draftDTO.setId(draft.getId());
            draftDTO.setStatus(draft.getStatus());
            draftDTO.setStyle(draft.getStyle());
            draftDTO.setTeamSize(draft.getTeamSize());
            draftDTO.setScheduledAt(draft.getScheduledAt());
            draftDTO.setStartedAt(draft.getStartedAt());
            draftDTO.setCompletedAt(draft.getCompletedAt());
            dto.setDraft(draftDTO);
        }

        if (group.getAdmin() != null) {
            AdminDTO adminDTO = new AdminDTO();
            adminDTO.setId(group.getAdmin().getId());
            adminDTO.setUsername(group.getAdmin().getUsername());
            dto.setAdmin(adminDTO);
        }

        if (group.getSeason() != null) {
            SeasonDTO seasonDTO = new SeasonDTO();
            seasonDTO.setId(group.getSeason().getSeason());
            seasonDTO.setSeasonName(group.getSeason().getSeasonName());
            seasonDTO.setVersion(group.getSeason().getVersion());
            dto.setSeason(seasonDTO);
        }

        return dto;
    }

    public static GroupDTO fromEntity(Group group, List<PointRule> rules) {
        GroupDTO dto = fromEntity(group);
        if (rules != null) {
            dto.setPointRules(rules.stream().map(r -> {
                PointRuleDTO p = new PointRuleDTO();
                p.setRuleType(r.getRuleType().name());
                p.setPoints(r.getPoints());
                return p;
            }).collect(Collectors.toList()));
        } else {
            dto.setPointRules(Collections.emptyList());
        }
        return dto;
    }
}
