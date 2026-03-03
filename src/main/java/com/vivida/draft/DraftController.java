package com.vivida.draft;

import com.vivida.auth.User;
import com.vivida.game.castaway.CastawayPerformanceDTO;
import com.vivida.social.group.GroupDTO;
import com.vivida.social.group.GroupRepository;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/v1/draft")
public class DraftController {

    private final DraftService draftService;
    private final GroupRepository groupRepository;

    public DraftController(DraftService draftService, GroupRepository groupRepository) {
        this.draftService = draftService;
        this.groupRepository = groupRepository;
    }

    /**
     * Start the draft for a group
     * POST /api/v1/draft/{groupId}/start
     */
    @PostMapping("{groupId}/start")
    public DraftStateDTO startDraft(@PathVariable Integer groupId) {
        return draftService.startDraft(groupId);
    }

    /**
     * Get current draft state
     * GET /api/v1/draft/{groupId}/state
     */
    @GetMapping("{groupId}/state")
    public DraftStateDTO getDraftState(@PathVariable Integer groupId) {
        return draftService.getDraftState(groupId);
    }

    /**
     * Make a draft pick
     * POST /api/v1/draft/{groupId}/pick
     * Body: { "castawayPerformanceId": 123 }
     */
    @PostMapping("{groupId}/pick")
    public DraftStateDTO makePick(
            @PathVariable Integer groupId,
            @RequestBody DraftPickRequest request,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return draftService.makePick(groupId, user.getId(), request.castawayPerformanceId);
    }

    /**
     * Get list of undrafted castaways
     * GET /api/v1/draft/{groupId}/undrafted
     */
    @GetMapping("{groupId}/undrafted")
    public List<CastawayPerformanceDTO> getUndraftedCastaways(@PathVariable Integer groupId) {
        return draftService.getUndraftedCastaways(groupId).stream()
                .map(CastawayPerformanceDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Check if it's the requesting user's turn
     * GET /api/v1/draft/{groupId}/my-turn
     */
    @GetMapping("{groupId}/my-turn")
    public Map<String, Object> isMyTurn(
            @PathVariable Integer groupId,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        boolean isMyTurn = draftService.isUserTurn(groupId, user.getId());
        
        Map<String, Object> response = new HashMap<>();
        response.put("isMyTurn", isMyTurn);
        
        if (isMyTurn) {
            DraftStateDTO state = draftService.getDraftState(groupId);
            response.put("pickNumber", state.currentPickNumber);
        }
        
        return response;
    }

    /**
     * Manually complete the draft (admin only)
     * POST /api/v1/draft/{groupId}/complete
     */
    @PostMapping("{groupId}/complete")
    public GroupDTO completeDraft(@PathVariable Integer groupId) {
        draftService.completeDraft(groupId);
        // Return updated group
        DraftStateDTO state = draftService.getDraftState(groupId);
        return state.group;
    }

    /**
     * Reset the draft - removes all picks and returns to PENDING status (admin only)
     * POST /api/v1/draft/{groupId}/reset
     */
    @PostMapping("{groupId}/reset")
    public GroupDTO resetDraft(@PathVariable Integer groupId) {
        draftService.resetDraft(groupId);
        // Return updated group by fetching it from the repository
        var groupEntity = groupRepository.findById(groupId).orElse(null);
        if (groupEntity != null) {
            return GroupDTO.fromEntity(groupEntity);
        }
        // If group not found, return empty DTO
        return new GroupDTO();
    }

    /**
     * Request body for making a draft pick
     */
    public static class DraftPickRequest {
        public Integer castawayPerformanceId;
    }
}
