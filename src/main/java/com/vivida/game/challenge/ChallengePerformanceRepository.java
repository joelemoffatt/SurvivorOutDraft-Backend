package com.vivida.game.challenge;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChallengePerformanceRepository extends JpaRepository<ChallengePerformance, Integer> {
    
    @Query("SELECT cp FROM ChallengePerformance cp JOIN cp.challenge c WHERE c.season.season = :seasonId")
    List<ChallengePerformance> findBySeasonId(@Param("seasonId") Integer seasonId);

    @Query("SELECT cp FROM ChallengePerformance cp WHERE cp.challenge.id = :challengeId")
    List<ChallengePerformance> findByChallengeId(@Param("challengeId") Integer challengeId);
}
