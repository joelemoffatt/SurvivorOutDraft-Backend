package com.vivida;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PointCalculationService {

    private static final boolean DEBUG = true;

    private final ChallengePerformanceRepository challengePerformanceRepository;
    private final VoteRepository voteRepository;
    private final BootRepository bootRepository;
    private final JuryVoteRepository juryVoteRepository;
    private final AdvantageMovementRepository advantageMovementRepository;
    private final PointRuleRepository pointRuleRepository;
    private final TeamCastawayRepository teamCastawayRepository;
    private final TeamService teamService;

    public PointCalculationService(
            ChallengePerformanceRepository challengePerformanceRepository,
            VoteRepository voteRepository,
            BootRepository bootRepository,
            JuryVoteRepository juryVoteRepository,
            AdvantageMovementRepository advantageMovementRepository,
            PointRuleRepository pointRuleRepository,
            TeamCastawayRepository teamCastawayRepository,
            TeamService teamService
    ) {
        this.challengePerformanceRepository = challengePerformanceRepository;
        this.voteRepository = voteRepository;
        this.bootRepository = bootRepository;
        this.juryVoteRepository = juryVoteRepository;
        this.advantageMovementRepository = advantageMovementRepository;
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

        debug("\n[Points] Team=" + teamCastaway.getTeam().getId()
                + " CastawayPerf=" + castawayPerformance.getId()
                + " Season=" + seasonId);
        
        int totalPoints = 0;
        List<PointRule> rules = pointRuleRepository.findByGroupIdAndActive(group.getId(), true);

        debug("[Points] Active rules=" + rules.size());

        for (PointRule rule : rules) {
            int ruleCount = countOccurrences(castawayPerformance, rule.getRuleType(), seasonId);
            totalPoints += ruleCount * rule.getPoints();
            debug("[Points] Rule=" + rule.getRuleType()
                    + " count=" + ruleCount
                    + " pointsEach=" + rule.getPoints()
                    + " subtotal=" + (ruleCount * rule.getPoints()));
        }

        teamCastaway.setPoints(totalPoints);
        teamCastawayRepository.save(teamCastaway);

        debug("[Points] Total points for TeamCastaway " + teamCastaway.getId() + " = " + totalPoints);
        
        // Recalculate team total points
        teamService.recalculateTeamPoints(teamCastaway.getTeam().getId());
    }

    /**
     * Calculate and update points for all castaways in a team
     */
    public void calculateAndUpdateTeamPoints(Integer teamId) {
        List<TeamCastaway> roster = teamCastawayRepository.findByTeamId(teamId);
        for (TeamCastaway teamCastaway : roster) {
            calculateAndUpdateTeamCastawayPoints(teamCastaway);
        }
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
            case MED_EVAC -> countMedEvac(castawayPerformance, seasonId);
            case QUIT -> countQuit(castawayPerformance, seasonId);
        };
    }

    private int countIndividualImmunity(CastawayPerformance castawayPerformance, Integer seasonId) {
        List<ChallengePerformance> performances = challengePerformanceRepository.findBySeasonId(seasonId);
        int castawayMatches = 0;
        int wonMatches = 0;
        int typeMatches = 0;
        for (ChallengePerformance cp : performances) {
            if (!cp.getCastaway().getId().equals(castawayPerformance.getId())) {
                continue;
            }
            castawayMatches++;
            if (cp.getWon() != null && cp.getWon()) {
                wonMatches++;
                String type = cp.getChallenge().getChallenge_type();
                if (type != null && (type.equals("Individual Immunity and Reward") || type.equals("Individual Immunity"))) {
                    typeMatches++;
                }
            }
        }

        debug("  [INDIVIDUAL_IMMUNITY] season=" + seasonId
                + " total=" + performances.size()
                + " castaway=" + castawayMatches
                + " won=" + wonMatches
                + " typeMatch=" + typeMatches);

        return typeMatches;
    }

    private int countFoundIdols(CastawayPerformance castawayPerformance, Integer seasonId) {
        List<AdvantageMovement> movements = advantageMovementRepository.findBySeasonId(seasonId);
        int castawayMatches = 0;
        int eventMatches = 0;
        int typeMatches = 0;
        for (AdvantageMovement am : movements) {
            if (!am.getCastawayId().getId().equals(castawayPerformance.getId())) {
                continue;
            }
            castawayMatches++;
            if (am.getEvent() != null && am.getEvent().equals("Found")) {
                eventMatches++;
                String type = am.getAdvantageType();
                if (type != null && type.toLowerCase().contains("idol")) {
                    typeMatches++;
                }
            }
        }

        debug("  [FOUND_IDOL] season=" + seasonId
                + " total=" + movements.size()
                + " castaway=" + castawayMatches
                + " eventFound=" + eventMatches
                + " idolType=" + typeMatches);

        return typeMatches;
    }

    private int countFoundAdvantages(CastawayPerformance castawayPerformance, Integer seasonId) {
        List<AdvantageMovement> movements = advantageMovementRepository.findBySeasonId(seasonId);
        int castawayMatches = 0;
        int eventMatches = 0;
        int typeMatches = 0;
        for (AdvantageMovement am : movements) {
            if (!am.getCastawayId().getId().equals(castawayPerformance.getId())) {
                continue;
            }
            castawayMatches++;
            if (am.getEvent() != null && am.getEvent().equals("Found")) {
                eventMatches++;
                String type = am.getAdvantageType();
                if (type != null && !type.toLowerCase().contains("idol")) {
                    typeMatches++;
                }
            }
        }

        debug("  [FOUND_ADVANTAGE] season=" + seasonId
                + " total=" + movements.size()
                + " castaway=" + castawayMatches
                + " eventFound=" + eventMatches
                + " nonIdolType=" + typeMatches);

        return typeMatches;
    }

    private int countPlayedIdolSuccessfully(CastawayPerformance castawayPerformance, Integer seasonId) {
        List<AdvantageMovement> movements = advantageMovementRepository.findBySeasonId(seasonId);
        int castawayMatches = 0;
        int eventMatches = 0;
        int typeMatches = 0;
        int successMatches = 0;
        for (AdvantageMovement am : movements) {
            if (!am.getCastawayId().getId().equals(castawayPerformance.getId())) {
                continue;
            }
            castawayMatches++;
            if (am.getEvent() != null && am.getEvent().equals("Played")) {
                eventMatches++;
                String type = am.getAdvantageType();
                if (type != null && type.toLowerCase().contains("idol")) {
                    typeMatches++;
                    if (am.getSuccess() != null && am.getSuccess().equalsIgnoreCase("true")) {
                        successMatches++;
                    }
                }
            }
        }

        debug("  [PLAYED_IDOL_SUCCESSFULLY] season=" + seasonId
                + " total=" + movements.size()
                + " castaway=" + castawayMatches
                + " eventPlayed=" + eventMatches
                + " idolType=" + typeMatches
                + " success=" + successMatches);

        return successMatches;
    }

    private int countPlayedAdvantageSuccessfully(CastawayPerformance castawayPerformance, Integer seasonId) {
        List<AdvantageMovement> movements = advantageMovementRepository.findBySeasonId(seasonId);
        int castawayMatches = 0;
        int eventMatches = 0;
        int typeMatches = 0;
        int successMatches = 0;
        for (AdvantageMovement am : movements) {
            if (!am.getCastawayId().getId().equals(castawayPerformance.getId())) {
                continue;
            }
            castawayMatches++;
            if (am.getEvent() != null && am.getEvent().equals("Played")) {
                eventMatches++;
                String type = am.getAdvantageType();
                if (type != null && !type.toLowerCase().contains("idol")) {
                    typeMatches++;
                    if (am.getSuccess() != null && am.getSuccess().equalsIgnoreCase("true")) {
                        successMatches++;
                    }
                }
            }
        }

        debug("  [PLAYED_ADVANTAGE_SUCCESSFULLY] season=" + seasonId
                + " total=" + movements.size()
                + " castaway=" + castawayMatches
                + " eventPlayed=" + eventMatches
                + " nonIdolType=" + typeMatches
                + " success=" + successMatches);

        return successMatches;
    }

    private boolean bootExistsForCastaway(CastawayPerformance castawayPerformance, Integer seasonId) {
        List<Boot> boots = bootRepository.findBySeasonId(seasonId);
        boolean exists = boots.stream()
            .anyMatch(boot -> boot.getCastaway().getId().equals(castawayPerformance.getId()));

        debug("  [BOOT_EXISTS] season=" + seasonId
            + " total=" + boots.size()
            + " castaway=" + castawayPerformance.getId()
            + " exists=" + exists);

        return exists;
    }

    private int countSoleSurvivor(CastawayPerformance castawayPerformance, Integer seasonId) {
        if (!bootExistsForCastaway(castawayPerformance, seasonId)) {
            return 0;
        }
        List<Boot> boots = bootRepository.findBySeasonId(seasonId);
        int castawayMatches = 0;
        int eventMatches = 0;
        for (Boot boot : boots) {
            if (!boot.getCastaway().getId().equals(castawayPerformance.getId())) {
                continue;
            }
            castawayMatches++;
            if (boot.getEvent() != null && boot.getEvent().toLowerCase().equals("first")) {
                eventMatches++;
            }
        }

        debug("  [SOLE_SURVIVOR] season=" + seasonId
                + " total=" + boots.size()
                + " castaway=" + castawayMatches
                + " eventFirst=" + eventMatches);

        return eventMatches;
    }

    private int countRunnerUp(CastawayPerformance castawayPerformance, Integer seasonId) {
        if (!bootExistsForCastaway(castawayPerformance, seasonId)) {
            return 0;
        }
        List<Boot> boots = bootRepository.findBySeasonId(seasonId);
        int castawayMatches = 0;
        int eventMatches = 0;
        for (Boot boot : boots) {
            if (!boot.getCastaway().getId().equals(castawayPerformance.getId())) {
                continue;
            }
            castawayMatches++;
            if (boot.getEvent() != null && boot.getEvent().toLowerCase().equals("second")) {
                eventMatches++;
            }
        }

        debug("  [RUNNER_UP] season=" + seasonId
                + " total=" + boots.size()
                + " castaway=" + castawayMatches
                + " eventSecond=" + eventMatches);

        return eventMatches;
    }

    private int countFinalThreeBonus(CastawayPerformance castawayPerformance, Integer seasonId) {
        if (!bootExistsForCastaway(castawayPerformance, seasonId)) {
            return 0;
        }
        List<Boot> boots = bootRepository.findBySeasonId(seasonId);
        int castawayMatches = 0;
        int eventMatches = 0;
        for (Boot boot : boots) {
            if (!boot.getCastaway().getId().equals(castawayPerformance.getId())) {
                continue;
            }
            castawayMatches++;
            if (boot.getEvent() != null &&
                    (boot.getEvent().toLowerCase().equals("third")
                    || boot.getEvent().toLowerCase().equals("second")
                    || boot.getEvent().toLowerCase().equals("first"))) {
                eventMatches++;
            }
        }

        debug("  [FINAL_THREE_BONUS] season=" + seasonId
                + " total=" + boots.size()
                + " castaway=" + castawayMatches
                + " eventTop3=" + eventMatches);

        return eventMatches;
    }

    private int countMedEvac(CastawayPerformance castawayPerformance, Integer seasonId) {
        if (!bootExistsForCastaway(castawayPerformance, seasonId)) {
            return 0;
        }
        List<Boot> boots = bootRepository.findBySeasonId(seasonId);
        int castawayMatches = 0;
        int eventMatches = 0;
        for (Boot boot : boots) {
            if (!boot.getCastaway().getId().equals(castawayPerformance.getId())) {
                continue;
            }
            castawayMatches++;
            if (boot.getEvent() != null && boot.getEvent().toLowerCase().equals("medEvac")) {
                eventMatches++;
            }
        }

        debug("  [MED_EVAC] season=" + seasonId
                + " total=" + boots.size()
                + " castaway=" + castawayMatches
                + " eventMedEvac=" + eventMatches);

        return eventMatches;
    }

    private int countQuit(CastawayPerformance castawayPerformance, Integer seasonId) {
        if (!bootExistsForCastaway(castawayPerformance, seasonId)) {
            return 0;
        }
        List<Boot> boots = bootRepository.findBySeasonId(seasonId);
        int castawayMatches = 0;
        int eventMatches = 0;
        for (Boot boot : boots) {
            if (!boot.getCastaway().getId().equals(castawayPerformance.getId())) {
                continue;
            }
            castawayMatches++;
            if (boot.getEvent() != null && boot.getEvent().toLowerCase().equals("quit")) {
                eventMatches++;
            }
        }

        debug("  [QUIT] season=" + seasonId
                + " total=" + boots.size()
                + " castaway=" + castawayMatches
                + " eventQuit=" + eventMatches);

        return eventMatches;
    }

    private void debug(String message) {
        if (DEBUG) {
            System.out.println(message);
        }
    }
}
