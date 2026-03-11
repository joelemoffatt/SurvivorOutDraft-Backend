package com.vivida.social.group;

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
    private LocalDateTime draftDate;
    private LocalDateTime draftStartTime;
    private LocalDateTime draftEndTime;
    private Integer teamSize;
    private Integer latestEpisodeWatched;
    private String draftOrder;
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

    public static GroupDTO fromEntity(Group group) {
        GroupDTO dto = new GroupDTO();
        dto.setId(group.getId());
        dto.setName(group.getName());
        dto.setDraftDate(group.getDraftDate());
        dto.setDraftStartTime(group.getDraftStartTime());
        dto.setDraftEndTime(group.getDraftEndTime());
        dto.setTeamSize(group.getTeamSize());
        dto.setLatestEpisodeWatched(group.getLatestEpisodeWatched());
        dto.setDraftOrder(group.getDraftOrder());
        dto.setStatus(group.getStatus());
        dto.setCreatedAt(group.getCreatedAt());

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
