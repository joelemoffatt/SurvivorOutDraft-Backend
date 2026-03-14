package com.vivida.game.castaway;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface CastawayPerformanceRepository extends JpaRepository<CastawayPerformance, Integer> {
    
    @Query("SELECT cp FROM CastawayPerformance cp WHERE cp.season.season = :seasonId")
    List<CastawayPerformance> findBySeasonId(@Param("seasonId") Integer seasonId);

    @Query("SELECT COUNT(cp) FROM CastawayPerformance cp WHERE cp.season.season = :seasonId")
    long countBySeasonId(@Param("seasonId") Integer seasonId);

    @Query("SELECT cp FROM CastawayPerformance cp WHERE cp.id = :id AND cp.season.season = :seasonId")
    Optional<CastawayPerformance> findByIdAndSeasonId(@Param("id") Integer id, @Param("seasonId") Integer seasonId);

    @Query("SELECT cp FROM CastawayPerformance cp WHERE cp.castaway.id = :castawayId")
    List<CastawayPerformance> findByCastawayId(@Param("castawayId") Integer castawayId);
}
