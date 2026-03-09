package com.vivida.draft;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vivida.auth.User;
import com.vivida.game.castaway.CastawayPerformance;
import com.vivida.game.castaway.CastawayPerformanceDTO;
import com.vivida.game.castaway.CastawayPerformanceRepository;
import com.vivida.scoring.PointCalculationService;
import com.vivida.social.group.*;
import com.vivida.social.team.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class DraftService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final TeamRepository teamRepository;
    private final TeamCastawayRepository teamCastawayRepository;
    private final CastawayPerformanceRepository castawayPerformanceRepository;
    private final PointCalculationService pointCalculationService;
    private final ObjectMapper objectMapper;

    public DraftService(GroupRepository groupRepository,
                       GroupMemberRepository groupMemberRepository,
                       TeamRepository teamRepository,
                       TeamCastawayRepository teamCastawayRepository,
                       CastawayPerformanceRepository castawayPerformanceRepository,
                       PointCalculationService pointCalculationService) {
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.teamRepository = teamRepository;
        this.teamCastawayRepository = teamCastawayRepository;
        this.castawayPerformanceRepository = castawayPerformanceRepository;
        this.pointCalculationService = pointCalculationService;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Parse draft order JSON to list of user IDs
     */
    private List<Integer> parseDraftOrder(String draftOrderJson) {
        if (draftOrderJson == null || draftOrderJson.trim().isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(draftOrderJson, new TypeReference<List<Integer>>() {});
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to parse draft order");
        }
    }

    /**
     * Serialize list of user IDs to JSON
     */
    private String serializeDraftOrder(List<Integer> userIds) {
        try {
            return objectMapper.writeValueAsString(userIds);
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to serialize draft order");
        }
    }

    /**
     * Start the draft for a group
     * - Validates group is in PENDING status
     * - Randomizes member order
     * - Stores draft order as JSON
     * - Sets status to DRAFTING
     */
    public DraftStateDTO startDraft(Integer groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found"));

        // Validate group status
        if (group.getStatus() != GroupStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Draft already started or group is not in PENDING status");
        }

        // Validate team size is set
        if (group.getTeamSize() == null || group.getTeamSize() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Team size must be set before starting draft");
        }

        // Get all active group members
        List<GroupMember> members = groupMemberRepository.findByGroupId(groupId).stream()
                .filter(m -> m.getStatus() == com.vivida.social.group.MembershipStatus.ACCEPTED)
                .collect(Collectors.toList());

        if (members.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "No active members in group");
        }

        // Randomize member order
        Collections.shuffle(members);
        List<Integer> userIds = members.stream()
                .map(m -> m.getUser().getId())
                .collect(Collectors.toList());

        // Store draft order
        group.setDraftOrder(serializeDraftOrder(userIds));
        group.setDraftStartTime(LocalDateTime.now());
        group.setStatus(GroupStatus.DRAFTING);
        groupRepository.save(group);

        return getDraftState(groupId);
    }

    /**
     * Get complete draft state
     */
    public DraftStateDTO getDraftState(Integer groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found"));

        if (group.getDraftOrder() == null || group.getDraftOrder().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Draft has not been started");
        }

        List<Integer> userIds = parseDraftOrder(group.getDraftOrder());
        int totalPicksMade = teamCastawayRepository.countByTeamGroupId(groupId);
        int teamSize = group.getTeamSize() != null ? group.getTeamSize() : 0;
        int totalPicksNeeded = userIds.size() * teamSize;

        // Build draft order with pick counts
        List<DraftPositionDTO> draftOrder = buildDraftOrder(group, userIds, totalPicksMade, teamSize);

        // Get current turn
        DraftPositionDTO currentTurn = getCurrentTurnPosition(group, userIds, totalPicksMade);

        // Get all teams
        List<TeamDTO> teams = teamRepository.findByGroupId(groupId).stream()
                .map(TeamDTO::new)
                .collect(Collectors.toList());

        // Get undrafted castaways
        List<CastawayPerformanceDTO> undraftedCastaways = getUndraftedCastaways(groupId).stream()
                .map(CastawayPerformanceDTO::new)
                .collect(Collectors.toList());

        // Check if complete
        boolean isComplete = isDraftComplete(group);

        return new DraftStateDTO(
                GroupDTO.fromEntity(group),
                draftOrder,
                currentTurn,
                totalPicksMade + 1,
                totalPicksNeeded,
                teams,
                undraftedCastaways,
                isComplete
        );
    }

    /**
     * Build draft order with statistics for each position
     */
    private List<DraftPositionDTO> buildDraftOrder(Group group, List<Integer> userIds, 
                                                   int totalPicksMade, int teamSize) {
        List<DraftPositionDTO> draftOrder = new ArrayList<>();
        
        for (int i = 0; i < userIds.size(); i++) {
            Integer userId = userIds.get(i);
            
            // Get user's team
            Optional<Team> teamOpt = teamRepository.findByGroupIdAndUserId(group.getId(), userId);
            if (teamOpt.isEmpty()) {
                continue; // Skip if user doesn't have a team
            }
            
            Team team = teamOpt.get();
            int pickCount = teamCastawayRepository.findByTeamId(team.getId()).size();
            
            // Calculate next pick number
            Integer nextPickNumber = calculateNextPickForPosition(i, userIds.size(), totalPicksMade, teamSize);
            
            draftOrder.add(new DraftPositionDTO(i, team.getUser(), pickCount, nextPickNumber));
        }
        
        return draftOrder;
    }

    /**
     * Calculate when a position will pick next
     */
    private Integer calculateNextPickForPosition(int position, int numPlayers, 
                                                 int picksMade, int teamSize) {
        int maxPicks = numPlayers * teamSize;
        
        // Check each future pick
        for (int pickNum = picksMade + 1; pickNum <= maxPicks; pickNum++) {
            int pickPosition = calculatePositionForPickNumber(pickNum, numPlayers);
            if (pickPosition == position) {
                return pickNum;
            }
        }
        
        return null; // This position is done picking
    }

    /**
     * Get current turn position
     */
    private DraftPositionDTO getCurrentTurnPosition(Group group, List<Integer> userIds, int totalPicksMade) {
        if (isDraftComplete(group)) {
            return null;
        }

        int nextPickNumber = totalPicksMade + 1;
        int position = calculatePositionForPickNumber(nextPickNumber, userIds.size());
        Integer userId = userIds.get(position);

        Optional<Team> teamOpt = teamRepository.findByGroupIdAndUserId(group.getId(), userId);
        if (teamOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "User in draft order does not have a team");
        }

        Team team = teamOpt.get();
        int pickCount = teamCastawayRepository.findByTeamId(team.getId()).size();

        return new DraftPositionDTO(position, team.getUser(), pickCount, nextPickNumber);
    }

    /**
     * Calculate which position picks for a given pick number using snake draft
     */
    private int calculatePositionForPickNumber(int pickNumber, int numPlayers) {
        int round = (pickNumber - 1) / numPlayers;
        int position;
        
        if (round % 2 == 0) {
            // Even rounds: forward (0, 1, 2, 3)
            position = (pickNumber - 1) % numPlayers;
        } else {
            // Odd rounds: backward (3, 2, 1, 0)
            position = numPlayers - 1 - ((pickNumber - 1) % numPlayers);
        }
        
        return position;
    }

    /**
     * Get user whose turn it is
     */
    public User getCurrentTurn(Integer groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found"));

        if (isDraftComplete(group)) {
            return null;
        }

        List<Integer> userIds = parseDraftOrder(group.getDraftOrder());
        int totalPicks = teamCastawayRepository.countByTeamGroupId(groupId);
        int pickNumber = totalPicks + 1;
        
        int position = calculatePositionForPickNumber(pickNumber, userIds.size());
        Integer userId = userIds.get(position);

        Optional<Team> teamOpt = teamRepository.findByGroupIdAndUserId(group.getId(), userId);
        if (teamOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "User in draft order does not have a team");
        }

        return teamOpt.get().getUser();
    }

    /**
     * Make a draft pick
     */
    public DraftStateDTO makePick(Integer groupId, Integer userId, Integer castawayPerformanceId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found"));

        // Validate draft is in progress
        if (group.getStatus() != GroupStatus.DRAFTING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Draft is not in progress");
        }

        // Validate it's this user's turn
        User currentTurnUser = getCurrentTurn(groupId);
        if (currentTurnUser == null || !currentTurnUser.getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "It is not your turn to pick");
        }

        // Get user's team
        Team team = teamRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Team not found"));

        // Validate team not already full
        int currentRosterSize = teamCastawayRepository.findByTeamId(team.getId()).size();
        if (currentRosterSize >= group.getTeamSize()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Team is already full");
        }

        // Validate castaway exists and is for this season
        CastawayPerformance castawayPerformance = castawayPerformanceRepository.findById(castawayPerformanceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Castaway not found"));

        if (!castawayPerformance.getSeason().getSeason().equals(group.getSeason().getSeason())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Castaway is not from this season");
        }

        // Validate same team cannot draft the same castaway twice
        if (teamCastawayRepository.existsByTeamIdAndCastawayPerformanceId(team.getId(), castawayPerformanceId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Your team already drafted this castaway");
        }

        // Validate per-castaway global cap in this group.
        // Cap increases dynamically: 1, then 2, then 3... only when all castaways reached prior cap.
        int maxAllowedPicks = getCurrentMaxAllowedPicks(groupId, group.getSeason().getSeason());
        int currentCastawayPickCount = teamCastawayRepository
                .countByGroupIdAndCastawayPerformanceId(groupId, castawayPerformanceId);
        if (currentCastawayPickCount >= maxAllowedPicks) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Castaway has reached max draft limit (" + maxAllowedPicks + ")"
            );
        }

        // Calculate pick number
        int pickNumber = teamCastawayRepository.countByTeamGroupId(groupId) + 1;

        // Create TeamCastaway
        TeamCastaway teamCastaway = new TeamCastaway();
        teamCastaway.setTeam(team);
        teamCastaway.setCastawayPerformance(castawayPerformance);
        teamCastaway.setDraftOrder(pickNumber);
        teamCastaway.setPoints(0); // TODO: Calculate initial points based on group settings if needed
        teamCastawayRepository.save(teamCastaway);

        // Check if draft should auto-complete
        if (isDraftComplete(group)) {
            completeDraft(groupId);
        }

        return getDraftState(groupId);
    }

    /**
     * Get list of available castaways using dynamic global cap.
     * Cap increases from 1 -> 2 -> 3... as full reactivation cycles complete.
     */
    public List<CastawayPerformance> getUndraftedCastaways(Integer groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found"));

        // Get all castaways for this season
        List<CastawayPerformance> allCastaways = castawayPerformanceRepository
                .findBySeasonId(group.getSeason().getSeason());

        // Count current picks for each castaway in this group
        Map<Integer, Long> castawayPickCounts = teamCastawayRepository.findByTeamGroupIdOrderByDraftOrderAsc(groupId)
            .stream()
            .collect(Collectors.groupingBy(tc -> tc.getCastawayPerformance().getId(), Collectors.counting()));

        int maxAllowedPicks = calculateCurrentMaxAllowedPicks(allCastaways, castawayPickCounts);

        return allCastaways.stream()
            .filter(cp -> castawayPickCounts.getOrDefault(cp.getId(), 0L) < maxAllowedPicks)
                .collect(Collectors.toList());
    }

    /**
     * Calculate dynamic max picks per castaway for current draft state.
     * Uses minimum pick count among all castaways + 1.
     */
    private int getCurrentMaxAllowedPicks(Integer groupId, Integer seasonId) {
        List<CastawayPerformance> allCastaways = castawayPerformanceRepository.findBySeasonId(seasonId);

        Map<Integer, Long> castawayPickCounts = teamCastawayRepository.findByTeamGroupIdOrderByDraftOrderAsc(groupId)
                .stream()
                .collect(Collectors.groupingBy(tc -> tc.getCastawayPerformance().getId(), Collectors.counting()));

        return calculateCurrentMaxAllowedPicks(allCastaways, castawayPickCounts);
    }

    private int calculateCurrentMaxAllowedPicks(List<CastawayPerformance> allCastaways, Map<Integer, Long> castawayPickCounts) {
        if (allCastaways.isEmpty()) {
            return 1;
        }

        long minPickCount = allCastaways.stream()
                .mapToLong(cp -> castawayPickCounts.getOrDefault(cp.getId(), 0L))
                .min()
                .orElse(0L);

        return (int) minPickCount + 1;
    }

    /**
     * Check if draft is complete
     */
    public boolean isDraftComplete(Group group) {
        if (group.getTeamSize() == null || group.getTeamSize() <= 0) {
            return false;
        }

        List<Team> teams = teamRepository.findByGroupId(group.getId());
        
        // Check if all teams have full rosters
        for (Team team : teams) {
            int rosterSize = teamCastawayRepository.findByTeamId(team.getId()).size();
            if (rosterSize < group.getTeamSize()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Complete the draft
     */
    public void completeDraft(Integer groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found"));

        // Recalculate points for all teams now that draft is finalized
        List<Team> teams = teamRepository.findByGroupId(groupId);
        for (Team team : teams) {
            pointCalculationService.calculateAndUpdateTeamPoints(team.getId());
            // Todo: If group settings require, also calculate points from previous events for drafted castaways and add to team points
        }

        group.setStatus(GroupStatus.ACTIVE);
        group.setDraftEndTime(LocalDateTime.now());
        groupRepository.save(group);
    }

    /**
     * Reset the draft - removes all picks and resets group to PENDING status
     */
    public void resetDraft(Integer groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found"));

        // Delete all draft picks for teams in this group
        List<Team> teams = teamRepository.findByGroupId(groupId);
        for (Team team : teams) {
            List<TeamCastaway> picks = teamCastawayRepository.findByTeamId(team.getId());
            teamCastawayRepository.deleteAll(picks);
        }

        // Reset group status back to PENDING
        group.setStatus(GroupStatus.PENDING);
        group.setDraftStartTime(null);
        group.setDraftEndTime(null);
        groupRepository.save(group);
    }

    /**
     * Check if it's a specific user's turn
     */
    public boolean isUserTurn(Integer groupId, Integer userId) {
        User currentTurn = getCurrentTurn(groupId);
        return currentTurn != null && currentTurn.getId().equals(userId);
    }
}
