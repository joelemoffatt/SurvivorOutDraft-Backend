package com.vivida.game.castaway;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface CastawayPerformanceRepository extends JpaRepository<CastawayPerformance, Integer> {
    
    @Query("SELECT cp FROM CastawayPerformance cp WHERE cp.season.season = :seasonId")
    List<CastawayPerformance> findBySeasonId(@Param("seasonId") Integer seasonId);
}
