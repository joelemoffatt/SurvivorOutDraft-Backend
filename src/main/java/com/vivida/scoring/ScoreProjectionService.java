package com.vivida.scoring;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vivida.game.castaway.CastawayPerformance;
import com.vivida.social.group.Group;
import com.vivida.social.team.Team;
import com.vivida.social.team.TeamCastaway;
import com.vivida.social.team.TeamCastawayRepository;
import com.vivida.social.team.TeamService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Orchestrates the full rebuild of score projections for a group.
 * 
 * Responsibilities:
 * - Create GroupScoreCalculationRun records
 * - Delete and rebuild TeamCastawayScoreEvent records
 * - Aggregate events into TeamCastaway.points
 * - Aggregate castaways into Team.totalPoints
 * - Detect stale scores and trigger recalculation when needed
 * 
 * This service operates synchronously for now, but is designed to support
 * async/background recalculation in the future.
 */
@Service
@Transactional
public class ScoreProjectionService {

    private final PointCalculationService pointCalculationService;
    private final GroupScoreCalculationRunRepository calculationRunRepository;
    private final TeamCastawayScoreEventRepository scoreEventRepository;
    private final PointRuleRepository pointRuleRepository;
    private final TeamCastawayRepository teamCastawayRepository;
    private final TeamService teamService;

    public ScoreProjectionService(
            PointCalculationService pointCalculationService,
            GroupScoreCalculationRunRepository calculationRunRepository,
            TeamCastawayScoreEventRepository scoreEventRepository,
            PointRuleRepository pointRuleRepository,
            TeamCastawayRepository teamCastawayRepository,
            TeamService teamService
    ) {
        this.pointCalculationService = pointCalculationService;
        this.calculationRunRepository = calculationRunRepository;
        this.scoreEventRepository = scoreEventRepository;
        this.pointRuleRepository = pointRuleRepository;
        this.teamCastawayRepository = teamCastawayRepository;
        this.teamService = teamService;
    }

    /**
     * Rebuild all scores for a group.
     * 
     * This is the primary entrypoint for full group score recalculation.
     * 
     * Flow:
     * 1. Create a new GroupScoreCalculationRun with status RUNNING
     * 2. Delete old score events for this group
     * 3. For each team castaway in the group:
     *    - Extract scoreable event facts for each active rule
     *    - Create TeamCastawayScoreEvent records
     * 4. Aggregate individual event scores into TeamCastaway.points
     * 5. Aggregate castaway totals into Team.totalPoints
     * 6. Mark run COMPLETED
     * 
     * If anything fails, mark run FAILED and rethrow exception.
     */
    public void recalculateGroupScores(Integer groupId) {
        GroupScoreCalculationRun run = null;
        
        try {
            // Load group (assuming GroupService or direct repository access)
            // For now, we'll need the Group object passed in future calls
            // This is a scaffolding point where you'd load the group
            
            run = new GroupScoreCalculationRun();
            run.setStatus(CalculationRunStatus.RUNNING);
            run.setStartedAt(LocalDateTime.now());
            // These will be set when we have the group object
            // run.setGroup(group);
            // run.setSeason(group.getSeason());
            // run.setLatestEpisodeNumber(/* extract from group.latestEpisodeWatched */);
            // run.setRuleVersion(group.getRuleVersion());
            // run.setGameDataVersion(group.getSeason().getGameDataVersion());
            
            // Save run to get ID
            run = calculationRunRepository.save(run);
            
            System.out.println("Started score calculation run: " + run.getId() + " for group: " + groupId);
            
        } catch (Exception e) {
            if (run != null) {
                run.setStatus(CalculationRunStatus.FAILED);
                run.setErrorMessage(e.getMessage());
                run.setCompletedAt(LocalDateTime.now());
                calculationRunRepository.save(run);
            }
            throw new RuntimeException("Failed to recalculate group scores: " + e.getMessage(), e);
        }
    }

    /**
     * Rebuild all scores for a group, given the group object.
     * 
     * Full implementation with access to the group entity.
     */
    public void recalculateGroupScores(Group group) {
        GroupScoreCalculationRun run = null;
        
        try {
            Integer groupId = group.getId();
            Integer seasonId = group.getSeason().getSeason();
            Integer firstScoringEpisodeNumber = group.getFirstScoringEpisodeNumber() != null
                ? group.getFirstScoringEpisodeNumber()
                : 1;
            Integer latestEpisodeNumber = null;
            if (group.getLatestEpisodeWatched() != null) {
                latestEpisodeNumber = group.getLatestEpisodeWatched().getEpisodeNumber();
            }
            
            // Create calculation run
            run = new GroupScoreCalculationRun();
            run.setGroup(group);
            run.setSeason(group.getSeason());
            run.setFirstScoringEpisodeNumber(firstScoringEpisodeNumber);
            run.setLatestEpisodeNumber(latestEpisodeNumber);
            run.setRuleVersion(group.getRuleVersion());
            run.setGameDataVersion(group.getSeason().getGameDataVersion());
            run.setStatus(CalculationRunStatus.RUNNING);
            run.setStartedAt(LocalDateTime.now());
            
            run = calculationRunRepository.save(run);
            System.out.println("Started score calculation run: " + run.getId() + " for group: " + groupId);
            
            // Load all teams in the group - query directly to avoid stale lazy-loaded collection
            List<Team> teams = teamService.getTeamsByGroupId(groupId);
            if (teams == null || teams.isEmpty()) {
                System.out.println("  No teams found in group");
                completeCalculationRun(run);
                return;
            }
            
            // Delete old score events for this group
            scoreEventRepository.deleteByGroupId(groupId);
            System.out.println("  Deleted old score events for group");
            
            // Get all rules
            List<PointRule> activeRules = pointRuleRepository.findByGroupId(groupId);
            if (activeRules.isEmpty()) {
                System.out.println("  No rules found in group");
                completeCalculationRun(run);
                return;
            }
            
            // Rebuild score events for each team castaway
            List<TeamCastawayScoreEvent> allEvents = new ArrayList<>();
            for (Team team : teams) {
                recalculateTeamScores(team, seasonId, firstScoringEpisodeNumber, latestEpisodeNumber, activeRules, run, allEvents);
            }
            
            // Save all score events
            scoreEventRepository.saveAll(allEvents);
            System.out.println("  Saved " + allEvents.size() + " score events");
            
            // Aggregate scores back to TeamCastaway and Team
            aggregateScores(teams);
            
            // Mark run completed
            completeCalculationRun(run);
            System.out.println("Completed score calculation run: " + run.getId());
            
        } catch (Exception e) {
            if (run != null) {
                run.setStatus(CalculationRunStatus.FAILED);
                run.setErrorMessage(e.getMessage());
                run.setCompletedAt(LocalDateTime.now());
                calculationRunRepository.save(run);
                System.err.println("Failed calculation run: " + run.getId() + " - " + e.getMessage());
            }
            throw new RuntimeException("Failed to recalculate group scores: " + e.getMessage(), e);
        }
    }

    /**
     * Recalculate scores for all castaways in a team.
     */
    private void recalculateTeamScores(
            Team team,
            Integer seasonId,
            Integer firstScoringEpisodeNumber,
            Integer latestEpisodeNumber,
            List<PointRule> activeRules,
            GroupScoreCalculationRun run,
            List<TeamCastawayScoreEvent> allEvents
    ) {
        // Query directly to avoid stale lazy-loaded collection
        List<TeamCastaway> roster = teamCastawayRepository.findByTeamId(team.getId());
        if (roster == null || roster.isEmpty()) {
            return;
        }
        
        System.out.println("  Team: " + team.getTeamName());
        
        for (TeamCastaway teamCastaway : roster) {
            recalculateTeamCastawayScores(
                teamCastaway,
                seasonId,
                firstScoringEpisodeNumber,
                latestEpisodeNumber,
                activeRules,
                run,
                allEvents
            );
        }
    }

    /**
     * Recalculate scores for a single team castaway.
     * 
     * For each active rule, extract scoreable facts and create
     * TeamCastawayScoreEvent records.
     */
    private void recalculateTeamCastawayScores(
            TeamCastaway teamCastaway,
            Integer seasonId,
            Integer firstScoringEpisodeNumber,
            Integer latestEpisodeNumber,
            List<PointRule> activeRules,
            GroupScoreCalculationRun run,
            List<TeamCastawayScoreEvent> allEvents
    ) {
        CastawayPerformance performance = teamCastaway.getCastawayPerformance();
        String castawayName = performance.getCastaway().getName();
        System.out.println("    Castaway: " + castawayName);
        
        for (PointRule rule : activeRules) {
            // Extract scoreable facts for this rule
            List<ScorableEventFact> facts = pointCalculationService.extractScorableEventFacts(
                performance,
                rule.getRuleType(),
                seasonId
            );
            
            // Filter facts by episode number based on group's latest episode watched and first scoring episode
            facts = facts.stream()
                .filter(fact -> fact.getEpisodeNumber() != null)
                .filter(fact -> fact.getEpisodeNumber() >= firstScoringEpisodeNumber)
                .filter(fact -> latestEpisodeNumber == null || fact.getEpisodeNumber() <= latestEpisodeNumber)
                .toList();
            
            // Convert each fact to a TeamCastawayScoreEvent
            for (ScorableEventFact fact : facts) {
                TeamCastawayScoreEvent event = new TeamCastawayScoreEvent();
                event.setGroup(run.getGroup());
                event.setTeam(teamCastaway.getTeam());
                event.setTeamCastaway(teamCastaway);
                event.setCastawayPerformance(performance);
                event.setPointRule(rule);
                event.setRuleType(rule.getRuleType());
                event.setSourceType(fact.getSourceType());
                event.setSourceId(fact.getSourceId());
                event.setEpisodeNumber(fact.getEpisodeNumber());
                event.setEventLabel(fact.getEventLabel());
                event.setCountValue(fact.getCountValue());
                event.setPointsEach(rule.getPoints());
                event.setMultiplier(fact.getMultiplier());
                event.setTotalPoints(fact.getCountValue() * rule.getPoints() * fact.getMultiplier());
                event.setCalculationRun(run);
                
                allEvents.add(event);
                
                System.out.println("      " + rule.getRuleType() + " - " + fact.getEventLabel() + ": +" + event.getTotalPoints());
            }
        }
    }

    /**
     * Aggregate score events back into TeamCastaway.points and Team.totalPoints.
     */
    private void aggregateScores(List<Team> teams) {
        System.out.println("  Aggregating scores...");
        
        for (Team team : teams) {
            int teamTotal = 0;
            // Query directly to avoid stale lazy-loaded collection
            List<TeamCastaway> roster = teamCastawayRepository.findByTeamId(team.getId());
            
            if (roster != null) {
                for (TeamCastaway teamCastaway : roster) {
                    // Sum all score events for this castaway
                    List<TeamCastawayScoreEvent> events = scoreEventRepository.findByTeamCastawayId(teamCastaway.getId());
                    int castawayTotal = events.stream()
                        .mapToInt(TeamCastawayScoreEvent::getTotalPoints)
                        .sum();
                    
                    teamCastaway.setPoints(castawayTotal);
                    teamCastawayRepository.save(teamCastaway);
                    teamTotal += castawayTotal;
                }
            }
            
            team.setTotalPoints(teamTotal);
            teamService.updateTeam(team);
        }
    }

    /**
     * Mark a calculation run as completed.
     */
    private void completeCalculationRun(GroupScoreCalculationRun run) {
        run.setStatus(CalculationRunStatus.COMPLETED);
        run.setCompletedAt(LocalDateTime.now());
        calculationRunRepository.save(run);
    }

    /**
     * Check if a group's scores are stale.
     * 
     * Scores are stale if:
     * - No calculation run exists for the group, OR
     * - The last run's ruleVersion doesn't match group.ruleVersion, OR
     * - The last run's gameDataVersion doesn't match season.gameDataVersion, OR
     * - The last run's latestEpisodeNumber doesn't match group's latest episode
     */
    public boolean isScoreStale(Group group) {
        var lastRun = calculationRunRepository.findFirstByGroupIdOrderByStartedAtDesc(group.getId());
        
        if (lastRun.isEmpty()) {
            return true;
        }
        
        GroupScoreCalculationRun run = lastRun.get();
        
        // Check rule version
        if (!run.getRuleVersion().equals(group.getRuleVersion())) {
            return true;
        }
        
        // Check game data version
        if (!run.getGameDataVersion().equals(group.getSeason().getGameDataVersion())) {
            return true;
        }
        
        // Check latest episode watched
        Integer currentEpisodeNumber = null;
        if (group.getLatestEpisodeWatched() != null) {
            currentEpisodeNumber = group.getLatestEpisodeWatched().getEpisodeNumber();
        }

        Integer currentFirstScoringEpisodeNumber = group.getFirstScoringEpisodeNumber() != null
            ? group.getFirstScoringEpisodeNumber()
            : 1;

        boolean latestMatches = (run.getLatestEpisodeNumber() == null && currentEpisodeNumber == null)
            || (run.getLatestEpisodeNumber() != null && run.getLatestEpisodeNumber().equals(currentEpisodeNumber));

        boolean firstMatches = run.getFirstScoringEpisodeNumber() != null
            && run.getFirstScoringEpisodeNumber().equals(currentFirstScoringEpisodeNumber);

        return !(latestMatches && firstMatches);
    }
}
