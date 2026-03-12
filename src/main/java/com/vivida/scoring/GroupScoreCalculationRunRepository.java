package com.vivida.scoring;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupScoreCalculationRunRepository extends JpaRepository<GroupScoreCalculationRun, Integer> {
    List<GroupScoreCalculationRun> findByGroupIdOrderByStartedAtDesc(Integer groupId);
    Optional<GroupScoreCalculationRun> findFirstByGroupIdOrderByStartedAtDesc(Integer groupId);
    List<GroupScoreCalculationRun> findByGroupIdAndStatus(Integer groupId, CalculationRunStatus status);
}
