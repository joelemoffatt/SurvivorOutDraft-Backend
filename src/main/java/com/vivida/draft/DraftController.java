package com.vivida.draft;

import com.vivida.auth.User;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("api/v1/drafts")
public class DraftController {

    private final DraftService draftService;

    public DraftController(DraftService draftService) {
        this.draftService = draftService;
    }

    // ── Read ───────────────────────────────────────────────────────────────────

    /**
     * GET /api/v1/drafts/{draftId}
     * Get complete draft state by draft ID.
     */
    @GetMapping("{draftId}")
    public DraftDTO getDraft(@PathVariable Integer draftId) {
        return draftService.getDraft(draftId);
    }

    /**
     * GET /api/v1/drafts/group/{groupId}
     * Get the current draft for a group (active > pending > latest).
     * This is the primary endpoint the frontend will poll.
     */
    @GetMapping("group/{groupId}")
    public DraftDTO getDraftForGroup(@PathVariable Integer groupId) {
        return draftService.getDraftForGroup(groupId);
    }

    // ── Lifecycle ──────────────────────────────────────────────────────────────

    /**
     * POST /api/v1/drafts/{draftId}/start
     * Transition PENDING → DRAFTING. Creates participants, castaways snapshot,
     * and all pick slots up-front.
     */
    @PostMapping("{draftId}/start")
    public DraftDTO startDraft(@PathVariable Integer draftId,
                               Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return draftService.startDraft(draftId, user.getId());
    }

    @PostMapping("group/{groupId}/start")
    public DraftDTO startDraftForGroup(@PathVariable Integer groupId,
                                       Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return draftService.startDraftForGroup(groupId, user.getId());
    }

    /**
     * POST /api/v1/drafts/{draftId}/pick
     * Fill in the current pick slot with a castaway.
     * Body: { castawayPerformanceId }
     */
    @PostMapping("{draftId}/pick")
    public DraftDTO makePick(@PathVariable Integer draftId,
                             @RequestBody PickRequest request,
                             Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return draftService.makePick(draftId, user.getId(), request.castawayPerformanceId);
    }

    /**
     * POST /api/v1/drafts/{draftId}/complete
     * Manually mark the draft as complete (admin safety valve).
     */
    @PostMapping("{draftId}/complete")
    public DraftDTO completeDraft(@PathVariable Integer draftId,
                                  Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return draftService.completeDraftById(draftId, user.getId());
    }

    @PostMapping("group/{groupId}/complete")
    public DraftDTO completeDraftForGroup(@PathVariable Integer groupId,
                                          Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return draftService.completeDraftByGroup(groupId, user.getId());
    }

    /**
     * POST /api/v1/drafts/{draftId}/reset
     * Clear all picks and return to PENDING for reconfiguration.
     */
    @PostMapping("{draftId}/reset")
    public DraftDTO resetDraft(@PathVariable Integer draftId,
                               Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return draftService.resetDraft(draftId, user.getId());
    }

    @PostMapping("group/{groupId}/reset")
    public DraftDTO resetDraftForGroup(@PathVariable Integer groupId,
                                       Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return draftService.resetDraftByGroup(groupId, user.getId());
    }

    // ── Helper ─────────────────────────────────────────────────────────────────

    /**
     * GET /api/v1/drafts/{draftId}/my-turn
     * Quick check — is it the requesting user's turn?
     */
    @GetMapping("{draftId}/my-turn")
    public Map<String, Object> isMyTurn(@PathVariable Integer draftId,
                                        Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        DraftDTO draft = draftService.getDraft(draftId);
        boolean isMyTurn = draft.currentTurnUser != null
                && draft.currentTurnUser.id.equals(user.getId());
        return Map.of(
                "isMyTurn", isMyTurn,
                "currentPickNumber", draft.currentPickNumber != null ? draft.currentPickNumber : 0
        );
    }

    // ── Request bodies ─────────────────────────────────────────────────────────

    public static class PickRequest {
        public Integer castawayPerformanceId;
    }
}
