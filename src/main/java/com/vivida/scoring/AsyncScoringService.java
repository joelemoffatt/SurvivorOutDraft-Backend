package com.vivida.scoring;

import com.vivida.social.group.GroupRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Runs score recalculation in a background thread.
 *
 * Must be a separate Spring bean from DraftService so that the @Async proxy
 * applies correctly (Spring AOP cannot proxy self-invocation).
 *
 * Called via TransactionSynchronization.afterCommit() in DraftService so that
 * the new transaction started here can see the committed TeamCastaway rows.
 */
@Service
public class AsyncScoringService {

    private final ScoreProjectionService scoreProjectionService;
    private final GroupRepository groupRepository;

    public AsyncScoringService(ScoreProjectionService scoreProjectionService,
                               GroupRepository groupRepository) {
        this.scoreProjectionService = scoreProjectionService;
        this.groupRepository = groupRepository;
    }

    @Async
    @Transactional
    public void recalculateGroupScoresAsync(Integer groupId) {
        groupRepository.findById(groupId).ifPresent(group -> {
            scoreProjectionService.recalculateGroupScores(group);
            group.setLoading(false);
            group.setLoadingText(null);
            groupRepository.save(group);
        });
    }
}
