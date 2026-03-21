package com.vivida.draft;

import com.vivida.game.castaway.CastawayPerformance;
import com.vivida.social.group.Group;
import com.vivida.social.team.Team;
import com.vivida.auth.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Complete draft state DTO.
 * One object that contains everything needed to render the entire draft page.
 * Some fields (participants) are set once when the draft starts.
 * Others (picks, currentPickNumber, currentTurnUser) are updated as the draft proceeds.
 */
public class DraftDTO {

    // ── Core identity ──────────────────────────────────────────────────────────
    public Integer id;
    public GroupSummary group;
    public Integer seasonId;
    public String seasonName;
    public UserSummary createdBy;

    // ── State ─────────────────────────────────────────────────────────────────
    public DraftStatus status;
    public DraftStyle style;
    public boolean isComplete;

    // ── Timing ────────────────────────────────────────────────────────────────
    public LocalDateTime scheduledAt;
    public LocalDateTime startedAt;
    public LocalDateTime completedAt;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;

    // ── Set once on draft start ────────────────────────────────────────────────
    public Integer teamSize;
    public Integer totalParticipants;
    public Integer totalCastaways;
    public Integer totalPicks;
    public List<ParticipantDTO> participants;

    // ── Updated as the draft progresses ───────────────────────────────────────
    public Integer maxDraftsPerCastaway;
    public Integer currentPickNumber;
    public UserSummary currentTurnUser;
    public List<PickSlotDTO> picks;
    public List<DraftCastawayDTO> draftCastaways;

    // ═══════════════════════════════════════════════════════════════════════════
    // Nested DTOs
    // ═══════════════════════════════════════════════════════════════════════════

    public static class GroupSummary {
        public Integer id;
        public String name;

        public GroupSummary(Group g) {
            this.id = g.getId();
            this.name = g.getName();
        }
    }

    public static class UserSummary {
        public Integer id;
        public String username;

        public UserSummary(User u) {
            this.id = u.getId();
            this.username = u.getUsername();
        }
    }

    public static class TeamSummary {
        public Integer id;
        public String teamName;

        public TeamSummary(Team t) {
            this.id = t.getId();
            this.teamName = t.getTeamName();
        }
    }

    /** Castaway performance that belongs to this draft pool. */
    public static class DraftCastawayDTO {
        public Integer draftId;
        public Integer castawayPerformanceId;
        public String castawayName;

        // We could include more castaway details here if needed, but for now just the name is enough.

        public DraftCastawayDTO(Integer draftId, CastawayPerformance cp) {
            this.draftId = draftId;
            this.castawayPerformanceId = cp.getId();
            this.castawayName = cp.getCastaway().getName();
        }
    }

    /** One participant slot — position in the draft order */
    public static class ParticipantDTO {
        public Integer id;
        public Integer draftPosition;
        public UserSummary user;
        public Integer teamId;
        public String teamName;
        public Integer picksMade;
        public Integer maxDraftsPerCastaway;
        public boolean active;

        public ParticipantDTO(DraftParticipant dp) {
            this.id = dp.getId();
            this.draftPosition = dp.getDraftPosition();
            this.user = new UserSummary(dp.getUser());
            if (dp.getTeam() != null) {
                this.teamId = dp.getTeam().getId();
                this.teamName = dp.getTeam().getTeamName();
            }
            this.picksMade = dp.getPicksMade();
            this.maxDraftsPerCastaway = dp.getMaxDraftsPerCastaway();
            this.active = Boolean.TRUE.equals(dp.getActive());
        }
    }

    /**
     * A pre-created pick slot.
     * castawayPerformance is null until the pick is made.
     */
    public static class PickSlotDTO {
        public Integer id;
        public Integer pickNumber;
        public Integer roundNumber;
        public Integer draftPosition;
        public UserSummary user;
        public TeamSummary team;
        public boolean isPicked;

        // Filled once picked:
        public Integer castawayPerformanceId;
        public String castawayName;
        public LocalDateTime pickedAt;

        public PickSlotDTO(DraftPick dp) {
            this.id = dp.getId();
            this.pickNumber = dp.getPickNumber();
            this.roundNumber = dp.getRoundNumber();
            this.draftPosition = dp.getDraftPosition();
            this.user = new UserSummary(dp.getUser());
            this.team = new TeamSummary(dp.getTeam());
            this.isPicked = dp.getCastawayPerformance() != null;
            this.pickedAt = dp.getPickedAt();
            if (dp.getCastawayPerformance() != null) {
                CastawayPerformance cp = dp.getCastawayPerformance();
                this.castawayPerformanceId = cp.getId();
                this.castawayName = cp.getCastaway().getName();
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Factory
    // ═══════════════════════════════════════════════════════════════════════════

    public static DraftDTO from(Draft draft) {
        DraftDTO dto = new DraftDTO();
        dto.id = draft.getId();
        dto.group = new GroupSummary(draft.getGroup());
        dto.seasonId = draft.getSeason().getSeason();
        dto.seasonName = draft.getSeason().getSeasonName();
        if (draft.getCreatedBy() != null) {
            dto.createdBy = new UserSummary(draft.getCreatedBy());
        }
        dto.status = draft.getStatus();
        dto.style = draft.getStyle();
        dto.scheduledAt = draft.getScheduledAt();
        dto.startedAt = draft.getStartedAt();
        dto.completedAt = draft.getCompletedAt();
        dto.createdAt = draft.getCreatedAt();
        dto.updatedAt = draft.getUpdatedAt();
        dto.teamSize = draft.getTeamSize();
        dto.totalParticipants = draft.getTotalParticipants();
        dto.totalCastaways = draft.getTotalCastaways();
        dto.totalPicks = draft.getTotalPicks();
        dto.currentPickNumber = draft.getCurrentPickNumber();
        dto.maxDraftsPerCastaway = draft.getMaxDraftsPerCastaway();
        if (draft.getCurrentTurnUser() != null) {
            dto.currentTurnUser = new UserSummary(draft.getCurrentTurnUser());
        }
        dto.participants = draft.getParticipants().stream()
                .map(ParticipantDTO::new)
                .collect(Collectors.toList());
        dto.picks = draft.getPicks().stream()
                .map(PickSlotDTO::new)
                .collect(Collectors.toList());
        dto.draftCastaways = draft.getDraftCastaways().stream()
            .map(draftCastaway -> new DraftCastawayDTO(draft.getId(), draftCastaway.getCastawayPerformance()))
            .collect(Collectors.toList());
        dto.isComplete = draft.getStatus() == DraftStatus.COMPLETED;
        return dto;
    }
}
