package com.vivida;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/points")
public class PointCalculationController {

    private final PointCalculationService pointCalculationService;
    private final TeamCastawayRepository teamCastawayRepository;

    public PointCalculationController(
            PointCalculationService pointCalculationService,
            TeamCastawayRepository teamCastawayRepository
    ) {
        this.pointCalculationService = pointCalculationService;
        this.teamCastawayRepository = teamCastawayRepository;
    }

    /**
     * Calculate and update points for a single team castaway
     */
    @PostMapping("team-castaway/{teamCastawayId}/calculate")
    public void calculateTeamCastawayPoints(@PathVariable Integer teamCastawayId) {
        TeamCastaway teamCastaway = teamCastawayRepository.findById(teamCastawayId)
                .orElseThrow(() -> new RuntimeException("TeamCastaway not found"));
        pointCalculationService.calculateAndUpdateTeamCastawayPoints(teamCastaway);
    }

    /**
     * Calculate and update points for all castaways in a team
     */
    @PostMapping("team/{teamId}/calculate")
    public void calculateTeamPoints(@PathVariable Integer teamId) {
        pointCalculationService.calculateAndUpdateTeamPoints(teamId);
    }

    /**
     * Calculate and update points for all teams in a group
     */
    @PostMapping("group/{groupId}/calculate")
    public void calculateGroupPoints(@PathVariable Integer groupId) {
        pointCalculationService.calculateAndUpdateGroupPoints(groupId);
    }
}
