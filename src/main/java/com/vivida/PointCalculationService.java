package com.vivida;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PointCalculationService {

    private final ChallengePerformanceRepository challengePerformanceRepository;
    @SuppressWarnings("unused")
    private final VoteRepository voteRepository;
    private final BootRepository bootRepository;
    @SuppressWarnings("unused")
    private final JuryVoteRepository juryVoteRepository;
    private final AdvantageMovementRepository advantageMovementRepository;
    private final TribeMappingRepository tribeMappingRepository;
    private final PointRuleRepository pointRuleRepository;
    private final TeamCastawayRepository teamCastawayRepository;
    private final TeamService teamService;

    public PointCalculationService(
            ChallengePerformanceRepository challengePerformanceRepository,
            VoteRepository voteRepository,
            BootRepository bootRepository,
            JuryVoteRepository juryVoteRepository,
            AdvantageMovementRepository advantageMovementRepository,
            TribeMappingRepository tribeMappingRepository,
            PointRuleRepository pointRuleRepository,
            TeamCastawayRepository teamCastawayRepository,
            TeamService teamService
    ) {
        this.challengePerformanceRepository = challengePerformanceRepository;
        this.voteRepository = voteRepository;
        this.bootRepository = bootRepository;
        this.juryVoteRepository = juryVoteRepository;
        this.advantageMovementRepository = advantageMovementRepository;
        this.tribeMappingRepository = tribeMappingRepository;
        this.pointRuleRepository = pointRuleRepository;
        this.teamCastawayRepository = teamCastawayRepository;
        this.teamService = teamService;
    }

    /**
     * Calculate total points for a TeamCastaway based on group rules
     */
    public void calculateAndUpdateTeamCastawayPoints(TeamCastaway teamCastaway) {
        Group group = teamCastaway.getTeam().getGroup();
        CastawayPerformance castawayPerformance = teamCastaway.getCastawayPerformance();
        Integer seasonId = group.getSeason().getSeason();
        
        int totalPoints = 0;
        List<PointRule> rules = pointRuleRepository.findByGroupIdAndActive(group.getId(), true);

        String castawayName = castawayPerformance.getCastaway().getName();
        System.out.println("  Castaway: " + castawayName + " (perfId=" + castawayPerformance.getId() + ")");

        for (PointRule rule : rules) {
            int ruleCount = countOccurrences(castawayPerformance, rule.getRuleType(), seasonId);
            int subtotal = ruleCount * rule.getPoints();
            totalPoints += subtotal;
            System.out.println("    " + rule.getRuleType()
                    + ": count=" + ruleCount
                    + " pointsEach=" + rule.getPoints()
                    + " subtotal=" + subtotal);
        }

        teamCastaway.setPoints(totalPoints);
        teamCastawayRepository.save(teamCastaway);

        System.out.println("    Castaway total: " + totalPoints + "\n");
        
        // Recalculate team total points
        teamService.recalculateTeamPoints(teamCastaway.getTeam().getId());
    }

    /**
     * Calculate and update points for all castaways in a team
     */
    public void calculateAndUpdateTeamPoints(Integer teamId) {
        Team team = teamService.getTeamById(teamId);
        System.out.println("\nTeam: " + team.getTeamName() + " (id=" + teamId + ")");
        List<TeamCastaway> roster = teamCastawayRepository.findByTeamId(teamId);
        for (TeamCastaway teamCastaway : roster) {
            calculateAndUpdateTeamCastawayPoints(teamCastaway);
        }
        Team updatedTeam = teamService.getTeamById(teamId);
        System.out.println("Team total: " + updatedTeam.getTotalPoints() + "\n");
    }

    /**
     * Calculate and update points for all teams in a group
     */
    public void calculateAndUpdateGroupPoints(Integer groupId) {
        List<PointRule> rules = pointRuleRepository.findByGroupId(groupId);
        if (rules.isEmpty()) {
            return;
        }

        // Get all teams in group and recalculate for each
        // Note: You'll need to add a method in TeamRepository to find by groupId
        // For now, we'll update via individual team calls
    }

    /**
     * Count occurrences of a rule type for a specific castaway performance
     */
    private int countOccurrences(CastawayPerformance castawayPerformance, RuleType ruleType, Integer seasonId) {
        return switch (ruleType) {
            case INDIVIDUAL_IMMUNITY -> countIndividualImmunity(castawayPerformance, seasonId);
            case FOUND_IDOL -> countFoundIdols(castawayPerformance, seasonId);
            case FOUND_ADVANTAGE -> countFoundAdvantages(castawayPerformance, seasonId);
            case PLAYED_IDOL_SUCCESSFULLY -> countPlayedIdolSuccessfully(castawayPerformance, seasonId);
            case PLAYED_ADVANTAGE_SUCCESSFULLY -> countPlayedAdvantageSuccessfully(castawayPerformance, seasonId);
            case SOLE_SURVIVOR -> countSoleSurvivor(castawayPerformance, seasonId);
            case RUNNER_UP -> countRunnerUp(castawayPerformance, seasonId);
            case FINAL_THREE_BONUS -> countFinalThreeBonus(castawayPerformance, seasonId);
            case MADE_MERGE -> countMadeMerge(castawayPerformance, seasonId);
            case MED_EVAC -> countMedEvac(castawayPerformance, seasonId);
            case QUIT -> countQuit(castawayPerformance, seasonId);
        };
    }

    private int countMadeMerge(CastawayPerformance castawayPerformance, Integer seasonId) {
        long count = tribeMappingRepository.countMergedBySeasonAndCastawayPerformance(
                seasonId,
                castawayPerformance.getId());
        return count > 0 ? 1 : 0;
    }

    private int countIndividualImmunity(CastawayPerformance castawayPerformance, Integer seasonId) {
        return (int) challengePerformanceRepository.findBySeasonId(seasonId).stream()
                .filter(cp -> cp.getCastaway().getId().equals(castawayPerformance.getId()))
                .filter(cp -> cp.getWon() != null && cp.getWon())
                .filter(cp -> {
                    String type = cp.getChallenge().getChallenge_type();
                    return type != null && (type.equals("Individual Immunity and Reward") || type.equals("Individual Immunity"));
                })
                .count();
    }

    private int countFoundIdols(CastawayPerformance castawayPerformance, Integer seasonId) {
        return (int) advantageMovementRepository.findBySeasonId(seasonId).stream()
                .filter(am -> am.getCastawayId().getId().equals(castawayPerformance.getId()))
                .filter(am -> am.getEvent() != null && am.getEvent().equals("Found"))
                .filter(am -> am.getAdvantageType() != null && am.getAdvantageType().toLowerCase().contains("idol"))
                .count();
    }

    private int countFoundAdvantages(CastawayPerformance castawayPerformance, Integer seasonId) {
        return (int) advantageMovementRepository.findBySeasonId(seasonId).stream()
                .filter(am -> am.getCastawayId().getId().equals(castawayPerformance.getId()))
                .filter(am -> am.getEvent() != null && am.getEvent().equals("Found"))
                .filter(am -> am.getAdvantageType() != null && !am.getAdvantageType().toLowerCase().contains("idol"))
                .count();
    }

    private int countPlayedIdolSuccessfully(CastawayPerformance castawayPerformance, Integer seasonId) {
        return (int) advantageMovementRepository.findBySeasonId(seasonId).stream()
                .filter(am -> am.getCastawayId().getId().equals(castawayPerformance.getId()))
                .filter(am -> am.getEvent() != null && am.getEvent().equals("Played"))
                .filter(am -> am.getAdvantageType() != null && am.getAdvantageType().toLowerCase().contains("idol"))
                .filter(am -> am.getSuccess() != null && am.getSuccess().equalsIgnoreCase("true"))
                .count();
    }

    private int countPlayedAdvantageSuccessfully(CastawayPerformance castawayPerformance, Integer seasonId) {
        return (int) advantageMovementRepository.findBySeasonId(seasonId).stream()
                .filter(am -> am.getCastawayId().getId().equals(castawayPerformance.getId()))
                .filter(am -> am.getEvent() != null && am.getEvent().equals("Played"))
                .filter(am -> am.getAdvantageType() != null && !am.getAdvantageType().toLowerCase().contains("idol"))
                .filter(am -> am.getSuccess() != null && am.getSuccess().equalsIgnoreCase("true"))
                .count();
    }

    private boolean bootExistsForCastaway(CastawayPerformance castawayPerformance, Integer seasonId) {
        return bootRepository.findBySeasonId(seasonId).stream()
            .anyMatch(boot -> boot.getCastaway().getId().equals(castawayPerformance.getId()));
    }

    private int countSoleSurvivor(CastawayPerformance castawayPerformance, Integer seasonId) {
        if (!bootExistsForCastaway(castawayPerformance, seasonId)) {
            return 0;
        }
        return (int) bootRepository.findBySeasonId(seasonId).stream()
                .filter(boot -> boot.getCastaway().getId().equals(castawayPerformance.getId()))
                .filter(boot -> boot.getEvent() != null && boot.getEvent().toLowerCase().equals("first"))
                .count();
    }

    private int countRunnerUp(CastawayPerformance castawayPerformance, Integer seasonId) {
        if (!bootExistsForCastaway(castawayPerformance, seasonId)) {
            return 0;
        }
        return (int) bootRepository.findBySeasonId(seasonId).stream()
                .filter(boot -> boot.getCastaway().getId().equals(castawayPerformance.getId()))
                .filter(boot -> boot.getEvent() != null && boot.getEvent().toLowerCase().equals("second"))
                .count();
    }

    private int countFinalThreeBonus(CastawayPerformance castawayPerformance, Integer seasonId) {
        if (!bootExistsForCastaway(castawayPerformance, seasonId)) {
            return 0;
        }
        return (int) bootRepository.findBySeasonId(seasonId).stream()
                .filter(boot -> boot.getCastaway().getId().equals(castawayPerformance.getId()))
                .filter(boot -> boot.getEvent() != null &&
                    (boot.getEvent().toLowerCase().equals("third")
                    || boot.getEvent().toLowerCase().equals("second")
                    || boot.getEvent().toLowerCase().equals("first")))
                .count();
    }

    private int countMedEvac(CastawayPerformance castawayPerformance, Integer seasonId) {
        if (!bootExistsForCastaway(castawayPerformance, seasonId)) {
            return 0;
        }
        return (int) bootRepository.findBySeasonId(seasonId).stream()
                .filter(boot -> boot.getCastaway().getId().equals(castawayPerformance.getId()))
                .filter(boot -> boot.getEvent() != null && boot.getEvent().toLowerCase().equalsIgnoreCase("medEvac"))
                .count();
    }

    private int countQuit(CastawayPerformance castawayPerformance, Integer seasonId) {
        if (!bootExistsForCastaway(castawayPerformance, seasonId)) {
            return 0;
        }
        return (int) bootRepository.findBySeasonId(seasonId).stream()
                .filter(boot -> boot.getCastaway().getId().equals(castawayPerformance.getId()))
                .filter(boot -> boot.getEvent() != null && boot.getEvent().toLowerCase().equals("quit"))
                .count();
    }
}
