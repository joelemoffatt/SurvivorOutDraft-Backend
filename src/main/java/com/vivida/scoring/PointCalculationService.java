package com.vivida.scoring;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vivida.game.advantage.AdvantageMovementRepository;
import com.vivida.game.castaway.CastawayPerformance;
import com.vivida.social.group.Group;
import com.vivida.social.team.Team;
import com.vivida.social.team.TeamCastaway;
import com.vivida.social.team.TeamCastawayRepository;
import com.vivida.social.team.TeamService;
import com.vivida.game.boot.BootRepository;
import com.vivida.game.challenge.ChallengePerformanceRepository;
import com.vivida.game.juryVote.JuryVoteRepository;
import com.vivida.game.tribe.TribeMappingRepository;
import com.vivida.game.vote.VoteRepository;

import java.util.ArrayList;
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
        List<PointRule> rules = pointRuleRepository.findByGroupId(group.getId());

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
        return extractScorableEventFacts(castawayPerformance, ruleType, seasonId).size();
    }

    /**
     * Extract all scoreable event facts for a given rule type and castaway.
     * 
     * This method is the core extraction engine used by both the legacy
     * countOccurrences path and the new ScoreProjectionService.
     * 
     * Returns a list of ScorableEventFact objects representing each individual
     * scoreable occurrence, including source details needed for audit trails.
     */
    public List<ScorableEventFact> extractScorableEventFacts(
            CastawayPerformance castawayPerformance,
            RuleType ruleType,
            Integer seasonId) {
        return switch (ruleType) {
            case INDIVIDUAL_IMMUNITY -> extractIndividualImmunity(castawayPerformance, seasonId);
            case TRIBAL_IMMUNITY -> extractTribalImmunity(castawayPerformance, seasonId);
            case FOUND_IDOL -> extractFoundIdols(castawayPerformance, seasonId);
            case FOUND_ADVANTAGE -> extractFoundAdvantages(castawayPerformance, seasonId);
            case PLAYED_IDOL_SUCCESSFULLY -> extractPlayedIdolSuccessfully(castawayPerformance, seasonId);
            case PLAYED_ADVANTAGE_SUCCESSFULLY -> extractPlayedAdvantageSuccessfully(castawayPerformance, seasonId);
            case SOLE_SURVIVOR -> extractSoleSurvivor(castawayPerformance, seasonId);
            case RUNNER_UP -> extractRunnerUp(castawayPerformance, seasonId);
            case FINAL_THREE_BONUS -> extractFinalThreeBonus(castawayPerformance, seasonId);
            case MADE_MERGE -> extractMadeMerge(castawayPerformance, seasonId);
            case MED_EVAC -> extractMedEvac(castawayPerformance, seasonId);
            case QUIT -> extractQuit(castawayPerformance, seasonId);
        };
    }

    private List<ScorableEventFact> extractIndividualImmunity(CastawayPerformance castawayPerformance, Integer seasonId) {
        List<ScorableEventFact> facts = new ArrayList<>();
        challengePerformanceRepository.findBySeasonId(seasonId).stream()
                .filter(cp -> cp.getCastaway().getId().equals(castawayPerformance.getId()))
                .filter(cp -> cp.getWonIndividualImmunity() != null && cp.getWonIndividualImmunity())
                .forEach(cp -> facts.add(new ScorableEventFact(
                    RuleType.INDIVIDUAL_IMMUNITY,
                    ScoreEventSourceType.CHALLENGE_PERFORMANCE,
                    cp.getId(),
                    cp.getChallenge().getEpisode() != null ? cp.getChallenge().getEpisode().getEpisodeNumber() : null,
                    "Won individual immunity",
                    1,
                    null,  // pointsEach set later by projection service
                    1
                )));
        return facts;
    }

    private List<ScorableEventFact> extractTribalImmunity(CastawayPerformance castawayPerformance, Integer seasonId) {
        List<ScorableEventFact> facts = new ArrayList<>();
        challengePerformanceRepository.findBySeasonId(seasonId).stream()
                .filter(cp -> cp.getCastaway().getId().equals(castawayPerformance.getId()))
                .filter(cp -> cp.getChallenge() != null
                        && cp.getChallenge().getChallenge_type() != null
                && cp.getChallenge().getChallenge_type().toLowerCase().contains("tribal")
                && cp.getChallenge().getChallenge_type().toLowerCase().contains("immunity"))
                .filter(cp -> Boolean.TRUE.equals(cp.getWon()))
                .forEach(cp -> facts.add(new ScorableEventFact(
                    RuleType.TRIBAL_IMMUNITY,
                    ScoreEventSourceType.CHALLENGE_PERFORMANCE,
                    cp.getId(),
                    cp.getChallenge().getEpisode() != null ? cp.getChallenge().getEpisode().getEpisodeNumber() : null,
                    "Won tribal immunity",
                    1,
                    null,
                    1
                )));
        return facts;
    }

    private List<ScorableEventFact> extractFoundIdols(CastawayPerformance castawayPerformance, Integer seasonId) {
        List<ScorableEventFact> facts = new ArrayList<>();
        advantageMovementRepository.findBySeasonId(seasonId).stream()
                .filter(am -> am.getCastawayId().getId().equals(castawayPerformance.getId()))
                .filter(am -> am.getEvent() != null && am.getEvent().equals("Found"))
                .filter(am -> am.getAdvantageType() != null && am.getAdvantageType().toLowerCase().contains("idol"))
                .forEach(am -> facts.add(new ScorableEventFact(
                    RuleType.FOUND_IDOL,
                    ScoreEventSourceType.ADVANTAGE_MOVEMENT,
                    am.getId(),
                    am.getEpisode() != null ? am.getEpisode().getEpisodeNumber() : null,
                    "Found idol",
                    1,
                    null,
                    1
                )));
        return facts;
    }

    private List<ScorableEventFact> extractFoundAdvantages(CastawayPerformance castawayPerformance, Integer seasonId) {
        List<ScorableEventFact> facts = new ArrayList<>();
        advantageMovementRepository.findBySeasonId(seasonId).stream()
                .filter(am -> am.getCastawayId().getId().equals(castawayPerformance.getId()))
                .filter(am -> am.getEvent() != null && am.getEvent().equals("Found"))
                .filter(am -> am.getAdvantageType() != null && !am.getAdvantageType().toLowerCase().contains("idol"))
                .forEach(am -> facts.add(new ScorableEventFact(
                    RuleType.FOUND_ADVANTAGE,
                    ScoreEventSourceType.ADVANTAGE_MOVEMENT,
                    am.getId(),
                    am.getEpisode() != null ? am.getEpisode().getEpisodeNumber() : null,
                    "Found advantage",
                    1,
                    null,
                    1
                )));
        return facts;
    }

    private List<ScorableEventFact> extractPlayedIdolSuccessfully(CastawayPerformance castawayPerformance, Integer seasonId) {
        List<ScorableEventFact> facts = new ArrayList<>();
        advantageMovementRepository.findBySeasonId(seasonId).stream()
                .filter(am -> am.getCastawayId().getId().equals(castawayPerformance.getId()))
                .filter(am -> am.getEvent() != null && am.getEvent().equals("Played"))
                .filter(am -> am.getAdvantageType() != null && am.getAdvantageType().toLowerCase().contains("idol"))
                .filter(am -> am.getSuccess() != null && am.getSuccess().equalsIgnoreCase("true"))
                .forEach(am -> facts.add(new ScorableEventFact(
                    RuleType.PLAYED_IDOL_SUCCESSFULLY,
                    ScoreEventSourceType.ADVANTAGE_MOVEMENT,
                    am.getId(),
                    am.getEpisode() != null ? am.getEpisode().getEpisodeNumber() : null,
                    "Played idol successfully",
                    1,
                    null,
                    1
                )));
        return facts;
    }

    private List<ScorableEventFact> extractPlayedAdvantageSuccessfully(CastawayPerformance castawayPerformance, Integer seasonId) {
        List<ScorableEventFact> facts = new ArrayList<>();
        advantageMovementRepository.findBySeasonId(seasonId).stream()
                .filter(am -> am.getCastawayId().getId().equals(castawayPerformance.getId()))
                .filter(am -> am.getEvent() != null && am.getEvent().equals("Played"))
                .filter(am -> am.getAdvantageType() != null && !am.getAdvantageType().toLowerCase().contains("idol"))
                .filter(am -> am.getSuccess() != null && am.getSuccess().equalsIgnoreCase("true"))
                .forEach(am -> facts.add(new ScorableEventFact(
                    RuleType.PLAYED_ADVANTAGE_SUCCESSFULLY,
                    ScoreEventSourceType.ADVANTAGE_MOVEMENT,
                    am.getId(),
                    am.getEpisode() != null ? am.getEpisode().getEpisodeNumber() : null,
                    "Played advantage successfully",
                    1,
                    null,
                    1
                )));
        return facts;
    }

    private List<ScorableEventFact> extractSoleSurvivor(CastawayPerformance castawayPerformance, Integer seasonId) {
        List<ScorableEventFact> facts = new ArrayList<>();
        if (bootExistsForCastaway(castawayPerformance, seasonId)) {
            bootRepository.findBySeasonId(seasonId).stream()
                    .filter(boot -> boot.getCastaway().getId().equals(castawayPerformance.getId()))
                    .filter(boot -> boot.getEvent() != null && boot.getEvent().toLowerCase().equals("first"))
                    .forEach(boot -> facts.add(new ScorableEventFact(
                        RuleType.SOLE_SURVIVOR,
                        ScoreEventSourceType.BOOT,
                        boot.getId(),
                        boot.getEpisode() != null ? boot.getEpisode().getEpisodeNumber() : null,
                        "Sole Survivor",
                        1,
                        null,
                        1
                    )));
        }
        return facts;
    }

    private List<ScorableEventFact> extractRunnerUp(CastawayPerformance castawayPerformance, Integer seasonId) {
        List<ScorableEventFact> facts = new ArrayList<>();
        if (bootExistsForCastaway(castawayPerformance, seasonId)) {
            bootRepository.findBySeasonId(seasonId).stream()
                    .filter(boot -> boot.getCastaway().getId().equals(castawayPerformance.getId()))
                    .filter(boot -> boot.getEvent() != null && boot.getEvent().toLowerCase().equals("second"))
                    .forEach(boot -> facts.add(new ScorableEventFact(
                        RuleType.RUNNER_UP,
                        ScoreEventSourceType.BOOT,
                        boot.getId(),
                        boot.getEpisode() != null ? boot.getEpisode().getEpisodeNumber() : null,
                        "Runner Up",
                        1,
                        null,
                        1
                    )));
        }
        return facts;
    }

    private List<ScorableEventFact> extractFinalThreeBonus(CastawayPerformance castawayPerformance, Integer seasonId) {
        List<ScorableEventFact> facts = new ArrayList<>();
        if (bootExistsForCastaway(castawayPerformance, seasonId)) {
            bootRepository.findBySeasonId(seasonId).stream()
                    .filter(boot -> boot.getCastaway().getId().equals(castawayPerformance.getId()))
                    .filter(boot -> boot.getEvent() != null &&
                        (boot.getEvent().toLowerCase().equals("third")
                        || boot.getEvent().toLowerCase().equals("second")
                        || boot.getEvent().toLowerCase().equals("first")))
                    .forEach(boot -> facts.add(new ScorableEventFact(
                        RuleType.FINAL_THREE_BONUS,
                        ScoreEventSourceType.BOOT,
                        boot.getId(),
                        boot.getEpisode() != null ? boot.getEpisode().getEpisodeNumber() : null,
                        "Final Three Bonus",
                        1,
                        null,
                        1
                    )));
        }
        return facts;
    }

    private List<ScorableEventFact> extractMadeMerge(CastawayPerformance castawayPerformance, Integer seasonId) {
        List<ScorableEventFact> facts = new ArrayList<>();
        var mergedMappings = tribeMappingRepository.findMergedBySeasonAndCastawayPerformance(
                seasonId,
                castawayPerformance.getId());
        if (!mergedMappings.isEmpty()) {
            var firstMerged = mergedMappings.get(0);
            facts.add(new ScorableEventFact(
                RuleType.MADE_MERGE,
                ScoreEventSourceType.TRIBE_MAPPING,
                firstMerged.getId(),
                firstMerged.getEpisode() != null ? firstMerged.getEpisode().getEpisodeNumber() : null,
                "Made merge",
                1,
                null,
                1
            ));
        }
        return facts;
    }

    private List<ScorableEventFact> extractMedEvac(CastawayPerformance castawayPerformance, Integer seasonId) {
        List<ScorableEventFact> facts = new ArrayList<>();
        if (bootExistsForCastaway(castawayPerformance, seasonId)) {
            bootRepository.findBySeasonId(seasonId).stream()
                    .filter(boot -> boot.getCastaway().getId().equals(castawayPerformance.getId()))
                    .filter(boot -> boot.getEvent() != null && boot.getEvent().toLowerCase().equalsIgnoreCase("medEvac"))
                    .forEach(boot -> facts.add(new ScorableEventFact(
                        RuleType.MED_EVAC,
                        ScoreEventSourceType.BOOT,
                        boot.getId(),
                        boot.getEpisode() != null ? boot.getEpisode().getEpisodeNumber() : null,
                        "Medical evacuation",
                        1,
                        null,
                        1
                    )));
        }
        return facts;
    }

    private List<ScorableEventFact> extractQuit(CastawayPerformance castawayPerformance, Integer seasonId) {
        List<ScorableEventFact> facts = new ArrayList<>();
        if (bootExistsForCastaway(castawayPerformance, seasonId)) {
            bootRepository.findBySeasonId(seasonId).stream()
                    .filter(boot -> boot.getCastaway().getId().equals(castawayPerformance.getId()))
                    .filter(boot -> boot.getEvent() != null && boot.getEvent().toLowerCase().equals("quit"))
                    .forEach(boot -> facts.add(new ScorableEventFact(
                        RuleType.QUIT,
                        ScoreEventSourceType.BOOT,
                        boot.getId(),
                        boot.getEpisode() != null ? boot.getEpisode().getEpisodeNumber() : null,
                        "Quit",
                        1,
                        null,
                        1
                    )));
        }
        return facts;
    }

    private boolean bootExistsForCastaway(CastawayPerformance castawayPerformance, Integer seasonId) {
        return bootRepository.findBySeasonId(seasonId).stream()
            .anyMatch(boot -> boot.getCastaway().getId().equals(castawayPerformance.getId()));
    }
}
