package com.vivida.draft;

import com.vivida.game.castaway.CastawayPerformanceDTO;
import com.vivida.social.group.GroupDTO;
import com.vivida.social.team.TeamDTO;

import java.util.List;

/**
 * DTO for complete draft state
 * Includes all information needed to render the draft UI
 */
public class DraftStateDTO {
    public GroupDTO group;
    public List<DraftPositionDTO> draftOrder;       // All users in draft order
    public DraftPositionDTO currentTurn;            // Whose turn it is
    public Integer currentPickNumber;               // Current pick number (1-based)
    public Integer totalPicks;                      // Total picks that need to be made
    public List<TeamDTO> teams;                     // All teams and their rosters
    public List<CastawayPerformanceDTO> undraftedCastaways;  // Available castaways
    public boolean isComplete;                      // Whether draft is complete

    public DraftStateDTO() {
    }

    public DraftStateDTO(GroupDTO group, List<DraftPositionDTO> draftOrder, 
                        DraftPositionDTO currentTurn, Integer currentPickNumber,
                        Integer totalPicks, List<TeamDTO> teams, 
                        List<CastawayPerformanceDTO> undraftedCastaways, 
                        boolean isComplete) {
        this.group = group;
        this.draftOrder = draftOrder;
        this.currentTurn = currentTurn;
        this.currentPickNumber = currentPickNumber;
        this.totalPicks = totalPicks;
        this.teams = teams;
        this.undraftedCastaways = undraftedCastaways;
        this.isComplete = isComplete;
    }
}
