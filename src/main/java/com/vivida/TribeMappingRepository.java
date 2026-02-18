package com.vivida;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TribeMappingRepository extends JpaRepository<TribeMapping, Integer> {
    @Query("SELECT COUNT(tm) FROM TribeMapping tm WHERE tm.season.season = :seasonId " +
	    "AND tm.castawayPerformance.id = :castawayPerformanceId " +
	    "AND LOWER(tm.status) = 'merged'")
    long countMergedBySeasonAndCastawayPerformance(
	    @Param("seasonId") Integer seasonId,
	    @Param("castawayPerformanceId") Integer castawayPerformanceId);
}
