package com.vivida.scoring;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamCastawayScoreEventRepository extends JpaRepository<TeamCastawayScoreEvent, Integer> {
    List<TeamCastawayScoreEvent> findByGroupId(Integer groupId);
    List<TeamCastawayScoreEvent> findByTeamId(Integer teamId);
    List<TeamCastawayScoreEvent> findByTeamCastawayId(Integer teamCastawayId);
    List<TeamCastawayScoreEvent> findByCalculationRunId(Integer calculationRunId);
    void deleteByGroupId(Integer groupId);
}
