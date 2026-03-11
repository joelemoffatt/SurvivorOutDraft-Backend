package com.vivida.social.group;

import com.vivida.draft.Draft;
import com.vivida.draft.DraftStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

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
    private Integer teamSize;
    private Integer latestEpisodeWatched;
    private GroupStatus status;
    private LocalDateTime createdAt;

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
    public static class DraftRefDTO {
        private Integer id;
        private DraftStatus status;
        private LocalDateTime scheduledAt;
        private LocalDateTime startedAt;
        private LocalDateTime completedAt;
    }

    public static GroupDTO fromEntity(Group group) {
        GroupDTO dto = new GroupDTO();
        dto.setId(group.getId());
        dto.setName(group.getName());
        dto.setTeamSize(group.getTeamSize());
        dto.setLatestEpisodeWatched(group.getLatestEpisodeWatched());
        dto.setStatus(group.getStatus());
        dto.setCreatedAt(group.getCreatedAt());

        Draft draft = group.getDraft();
        if (draft != null) {
            DraftRefDTO draftDTO = new DraftRefDTO();
            draftDTO.setId(draft.getId());
            draftDTO.setStatus(draft.getStatus());
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
}
