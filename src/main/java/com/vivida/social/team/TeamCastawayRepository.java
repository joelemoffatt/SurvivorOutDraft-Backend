package com.vivida.social.team;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TeamCastawayRepository extends JpaRepository<TeamCastaway, Integer> {
    List<TeamCastaway> findByTeamId(Integer teamId);
    List<TeamCastaway> findByTeamIdOrderByDraftOrderAsc(Integer teamId);
    Optional<TeamCastaway> findByTeamIdAndCastawayPerformanceId(Integer teamId, Integer castawayPerformanceId);
    boolean existsByTeamIdAndCastawayPerformanceId(Integer teamId, Integer castawayPerformanceId);
    
    // Draft-related methods
    @Query("SELECT COUNT(tc) FROM TeamCastaway tc WHERE tc.team.group.id = :groupId")
    int countByTeamGroupId(@Param("groupId") Integer groupId);
    
    @Query("SELECT tc FROM TeamCastaway tc WHERE tc.team.group.id = :groupId ORDER BY tc.draftOrder ASC")
    List<TeamCastaway> findByTeamGroupIdOrderByDraftOrderAsc(@Param("groupId") Integer groupId);
    
    @Query("SELECT tc FROM TeamCastaway tc WHERE tc.team.group.id = :groupId AND tc.castawayPerformance.id = :castawayPerformanceId")
    Optional<TeamCastaway> findByGroupIdAndCastawayPerformanceId(@Param("groupId") Integer groupId, @Param("castawayPerformanceId") Integer castawayPerformanceId);
}
