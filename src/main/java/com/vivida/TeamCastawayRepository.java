package com.vivida;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamCastawayRepository extends JpaRepository<TeamCastaway, Integer> {
    List<TeamCastaway> findByTeamId(Integer teamId);
    List<TeamCastaway> findByTeamIdOrderByDraftOrderAsc(Integer teamId);
    Optional<TeamCastaway> findByTeamIdAndCastawayPerformanceId(Integer teamId, Integer castawayPerformanceId);
    boolean existsByTeamIdAndCastawayPerformanceId(Integer teamId, Integer castawayPerformanceId);
}
