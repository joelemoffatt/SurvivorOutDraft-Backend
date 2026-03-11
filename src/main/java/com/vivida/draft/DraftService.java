package com.vivida.draft;

import com.vivida.auth.User;
import com.vivida.game.boot.BootRepository;
import com.vivida.game.castaway.CastawayPerformance;
import com.vivida.game.castaway.CastawayPerformanceRepository;
import com.vivida.social.group.Group;
import com.vivida.social.group.GroupMember;
import com.vivida.social.group.GroupMemberRepository;
import com.vivida.social.group.GroupRepository;
import com.vivida.social.group.GroupStatus;
import com.vivida.social.group.MembershipStatus;
import com.vivida.social.team.Team;
import com.vivida.social.team.TeamRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class DraftService {

    private final DraftRepository draftRepository;
    private final DraftParticipantRepository participantRepository;
    private final DraftPickRepository pickRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final TeamRepository teamRepository;
    private final CastawayPerformanceRepository castawayPerformanceRepository;
    private final BootRepository bootRepository;

    public DraftService(DraftRepository draftRepository,
                        DraftParticipantRepository participantRepository,
                        DraftPickRepository pickRepository,
                        GroupRepository groupRepository,
                        GroupMemberRepository groupMemberRepository,
                        TeamRepository teamRepository,
                        CastawayPerformanceRepository castawayPerformanceRepository,
                        BootRepository bootRepository) {
        this.draftRepository = draftRepository;
        this.participantRepository = participantRepository;
        this.pickRepository = pickRepository;
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.teamRepository = teamRepository;
        this.castawayPerformanceRepository = castawayPerformanceRepository;
        this.bootRepository = bootRepository;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Create
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Create a new draft for a group in PENDING status.
     * No participants or picks are generated yet — that happens at startDraft().
     */
    public DraftDTO createDraft(CreateDraftRequest req, User requestingUser) {
        Group group = groupRepository.findById(req.getGroupId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found"));

        if (req.getTeamSize() == null || req.getTeamSize() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "teamSize must be a positive integer");
        }

        // Guard: only one active/pending draft per group
        boolean alreadyExists = draftRepository.existsByGroupIdAndStatus(group.getId(), DraftStatus.DRAFTING)
                || draftRepository.existsByGroupIdAndStatus(group.getId(), DraftStatus.PENDING);
        if (alreadyExists) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "A PENDING or DRAFTING draft already exists for this group");
        }

        Draft draft = new Draft();
        draft.setGroup(group);
        draft.setSeason(group.getSeason());
        draft.setCreatedBy(requestingUser);
        draft.setStatus(DraftStatus.PENDING);
        draft.setStyle(req.getStyle() != null ? req.getStyle() : DraftStyle.SNAKE);
        draft.setTeamSize(req.getTeamSize());
        draft.setScheduledAt(req.getScheduledAt());
        draft.setTotalParticipants(0);
        draft.setTotalCastaways(0);
        draft.setTotalPicks(0);
        draft.setCurrentPickNumber(1);

        draftRepository.save(draft);
        return DraftDTO.from(loadFull(draft.getId()));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Start
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Transition from PENDING → DRAFTING:
     *  1. Shuffle group members and create DraftParticipants
     *  2. Count available CastawayPerformances (no snapshot rows — frontend queries the pool)
     *  3. Pre-create all DraftPick slots based on style + teamSize
     *  4. Set currentTurnUser to the first picker
     */
    public DraftDTO startDraft(Integer draftId) {
        Draft draft = loadFull(draftId);

        if (draft.getStatus() != DraftStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Draft is not in PENDING status (current: " + draft.getStatus() + ")");
        }

        // ── 1. Participants from accepted group members ──────────────────────
        List<GroupMember> accepted = groupMemberRepository
                .findByGroupId(draft.getGroup().getId()).stream()
                .filter(m -> m.getStatus() == MembershipStatus.ACCEPTED)
                .collect(Collectors.toList());

        if (accepted.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No accepted members in group");
        }

        Collections.shuffle(accepted);

        List<DraftParticipant> participants = new ArrayList<>();
        for (int i = 0; i < accepted.size(); i++) {
            GroupMember gm = accepted.get(i);
            Team team = teamRepository.findByGroupIdAndUserId(draft.getGroup().getId(), gm.getUser().getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Member " + gm.getUser().getUsername() + " has no team in this group"));

            DraftParticipant dp = new DraftParticipant();
            dp.setDraft(draft);
            dp.setUser(gm.getUser());
            dp.setTeam(team);
            dp.setDraftPosition(i);
            dp.setPicksMade(0);
            dp.setActive(true);
            participants.add(dp);
        }
        participantRepository.saveAll(participants);
        draft.setParticipants(participants);

        // ── 2. Count the castaway pool (no snapshot rows needed) ─────────────
        List<CastawayPerformance> allCastaways = findAvailableCastawaysForDraft(draft);
        if (allCastaways.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No available castaways remain after applying watched-episode boots");
        }

        // ── 3. Pre-create all pick slots ─────────────────────────────────────
        int totalParticipants = participants.size();
        int totalPicks = totalParticipants * draft.getTeamSize();
        List<DraftPick> picks = new ArrayList<>();

        for (int pickNum = 1; pickNum <= totalPicks; pickNum++) {
            int position = calculatePosition(pickNum, totalParticipants, draft.getStyle());
            DraftParticipant participant = participants.get(position);
            int round = (pickNum - 1) / totalParticipants;

            DraftPick pick = new DraftPick();
            pick.setDraft(draft);
            pick.setPickNumber(pickNum);
            pick.setRoundNumber(round + 1); // 1-based
            pick.setDraftPosition(position);
            pick.setUser(participant.getUser());
            pick.setTeam(participant.getTeam());
            picks.add(pick);
        }
        pickRepository.saveAll(picks);
        draft.setPicks(picks);

        // ── 4. Finalise header ───────────────────────────────────────────────
        draft.setStatus(DraftStatus.DRAFTING);
        draft.setStartedAt(LocalDateTime.now());
        draft.setTotalParticipants(totalParticipants);
        draft.setTotalCastaways(allCastaways.size());
        draft.setTotalPicks(totalPicks);
        draft.setCurrentPickNumber(1);
        draft.setMaxDraftsPerCastaway(1);
        draft.setCurrentTurnUser(participants.get(0).getUser());

        // Also update the parent Group status
        // Todo: Consider removing unnecessary coupling between Group and Draft statuses — can a Group be in DRAFTING status without an active Draft?
        Group group = draft.getGroup();
        group.setStatus(GroupStatus.DRAFTING);
        group.setDraftStartTime(LocalDateTime.now());
        groupRepository.save(group);

        draftRepository.save(draft);
        return DraftDTO.from(loadFull(draftId));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Pick
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Fill in the current pick slot with a castaway.
     * Validates that:
     *  - draft is DRAFTING
     *  - userId is whose turn it currently is
     *  - castaway is in the draft pool and not yet drafted
     */
    public DraftDTO makePick(Integer draftId, Integer userId, Integer castawayPerformanceId) {
        Draft draft = loadFull(draftId);

        if (draft.getStatus() != DraftStatus.DRAFTING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Draft is not in progress");
        }

        // Validate turn
        if (draft.getCurrentTurnUser() == null || !draft.getCurrentTurnUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "It is not your turn");
        }

        // Validate the castaway exists in this season
        CastawayPerformance castawayPerformance = castawayPerformanceRepository
            .findByIdAndSeasonId(castawayPerformanceId, draft.getSeason().getSeason())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Castaway is not part of this draft's season"));

        if (isBootedInWatchedEpisodes(draft, castawayPerformanceId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "This castaway was booted in an episode your group has watched");
        }

        // Guard: same team cannot draft the same castaway twice
        DraftParticipant currentParticipant = participantRepository
            .findByDraftIdAndUserId(draftId, userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "User is not a participant in this draft"));
        Integer teamId = currentParticipant.getTeam().getId();

        if (pickRepository.existsByDraftIdAndTeamIdAndCastawayPerformanceId(draftId, teamId, castawayPerformanceId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Your team has already drafted this castaway");
        }

        // Guard: castaway has reached the global draft cap
        long timesDrafted = pickRepository.countByDraftIdAndCastawayPerformanceId(draftId, castawayPerformanceId);
        if (timesDrafted >= draft.getMaxDraftsPerCastaway()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "This castaway has already been drafted the maximum number of times (" +
                    draft.getMaxDraftsPerCastaway() + ")");
        }

        // Fill in the current pick slot
        DraftPick currentPick = pickRepository
                .findByDraftIdAndPickNumber(draftId, draft.getCurrentPickNumber())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Pick slot " + draft.getCurrentPickNumber() + " not found"));

        currentPick.setCastawayPerformance(castawayPerformance);
        currentPick.setPickedAt(LocalDateTime.now());
        pickRepository.save(currentPick);

        // Update participant pick count
        currentParticipant.setPicksMade(currentParticipant.getPicksMade() + 1);
        participantRepository.save(currentParticipant);

        // Advance pick number
        int nextPickNumber = draft.getCurrentPickNumber() + 1;
        draft.setCurrentPickNumber(nextPickNumber);
        // Maybe increment the per-castaway cap:
        // once every castaway has been drafted maxDraftsPerCastaway times, raise the cap by 1
        long totalFilledPicks = pickRepository.countByDraftIdAndCastawayPerformanceIsNotNull(draftId);
        if (draft.getTotalCastaways() > 0 && totalFilledPicks % draft.getTotalCastaways() == 0) {
            draft.setMaxDraftsPerCastaway(draft.getMaxDraftsPerCastaway() + 1);
        }


        if (nextPickNumber > draft.getTotalPicks()) {
            // All slots filled → auto-complete
            completeDraft(draft);
        } else {
            // Find who picks next
            DraftPick nextPick = pickRepository
                    .findByDraftIdAndPickNumber(draftId, nextPickNumber)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Next pick slot not found"));
            draft.setCurrentTurnUser(nextPick.getUser());
        }

        draftRepository.save(draft);
        return DraftDTO.from(loadFull(draftId));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Complete / Reset
    // ═══════════════════════════════════════════════════════════════════════════

    public DraftDTO completeDraftById(Integer draftId) {
        Draft draft = loadFull(draftId);
        completeDraft(draft);
        draftRepository.save(draft);
        return DraftDTO.from(loadFull(draftId));
    }

    private void completeDraft(Draft draft) {
        draft.setStatus(DraftStatus.COMPLETED);
        draft.setCompletedAt(LocalDateTime.now());
        draft.setCurrentTurnUser(null);

        Group group = draft.getGroup();
        group.setStatus(GroupStatus.ACTIVE);
        group.setDraftEndTime(LocalDateTime.now());
        groupRepository.save(group);
    }

    /** Clear all picks and castaways, return to PENDING for reconfiguration. */
    public DraftDTO resetDraft(Integer draftId) {
        Draft draft = loadFull(draftId);

        pickRepository.deleteAll(draft.getPicks());
        participantRepository.deleteAll(draft.getParticipants());

        draft.getPicks().clear();
        draft.getParticipants().clear();

        draft.setStatus(DraftStatus.PENDING);
        draft.setStartedAt(null);
        draft.setCompletedAt(null);
        draft.setMaxDraftsPerCastaway(1);
        draft.setCurrentPickNumber(1);
        draft.setCurrentTurnUser(null);
        draft.setTotalParticipants(0);
        draft.setTotalCastaways(0);
        draft.setTotalPicks(0);

        Group group = draft.getGroup();
        group.setStatus(GroupStatus.PENDING);
        group.setDraftStartTime(null);
        group.setDraftEndTime(null);
        groupRepository.save(group);

        draftRepository.save(draft);
        return DraftDTO.from(loadFull(draftId));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Read
    // ═══════════════════════════════════════════════════════════════════════════

    public DraftDTO getDraft(Integer draftId) {
        return DraftDTO.from(loadFull(draftId));
    }

    /**
     * Get the most recent draft for a group (PENDING or DRAFTING preferred,
     * otherwise the latest by createdAt).
     */
    public DraftDTO getDraftForGroup(Integer groupId) {
        // Prefer active one first
        Optional<Draft> active = draftRepository.findByGroupIdAndStatus(groupId, DraftStatus.DRAFTING);
        if (active.isPresent()) return DraftDTO.from(loadFull(active.get().getId()));

        Optional<Draft> pending = draftRepository.findByGroupIdAndStatus(groupId, DraftStatus.PENDING);
        if (pending.isPresent()) return DraftDTO.from(loadFull(pending.get().getId()));

        Draft latest = draftRepository.findFirstByGroupIdOrderByCreatedAtDesc(groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No draft found for group " + groupId));
        return DraftDTO.from(loadFull(latest.getId()));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Helpers
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Calculate which draft position (0-based) picks for a given pick number.
     */
    private int calculatePosition(int pickNumber, int numPlayers, DraftStyle style) {
        switch (style) {
            case SNAKE: {
                int round = (pickNumber - 1) / numPlayers;
                if (round % 2 == 0) {
                    return (pickNumber - 1) % numPlayers;
                } else {
                    return numPlayers - 1 - ((pickNumber - 1) % numPlayers);
                }
            }
            case ROUND_ROBIN:
            case LINEAR:
            default:
                return (pickNumber - 1) % numPlayers;
        }
    }

    /**
     * Load draft with all collections eagerly from DB.
     */
    private Draft loadFull(Integer draftId) {
        return draftRepository.findById(draftId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Draft not found: " + draftId));
    }

    private List<CastawayPerformance> findAvailableCastawaysForDraft(Draft draft) {
        List<CastawayPerformance> seasonCastaways = castawayPerformanceRepository
                .findBySeasonId(draft.getSeason().getSeason());

        Integer latestEpisodeWatched = draft.getGroup().getLatestEpisodeWatched();
        if (latestEpisodeWatched == null || latestEpisodeWatched <= 0) {
            return seasonCastaways;
        }

        Set<Integer> bootedCastawayPerformanceIds = new HashSet<>(
                bootRepository.findBySeasonIdAndEpisodeNumberLessThanEqual(
                                draft.getSeason().getSeason(),
                                latestEpisodeWatched)
                        .stream()
                        .map(boot -> boot.getCastaway().getId())
                        .toList()
        );

        return seasonCastaways.stream()
                .filter(cp -> !bootedCastawayPerformanceIds.contains(cp.getId()))
                .collect(Collectors.toList());
    }

    private boolean isBootedInWatchedEpisodes(Draft draft, Integer castawayPerformanceId) {
        Integer latestEpisodeWatched = draft.getGroup().getLatestEpisodeWatched();
        if (latestEpisodeWatched == null || latestEpisodeWatched <= 0) {
            return false;
        }
        return bootRepository.existsBySeasonIdAndEpisodeNumberLessThanEqualAndCastawayPerformanceId(
                draft.getSeason().getSeason(),
                latestEpisodeWatched,
                castawayPerformanceId
        );
    }
}
