package com.vivida.game.boot;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BootRepository extends JpaRepository<Boot, Integer> {
    
    @Query("SELECT b FROM Boot b WHERE b.episode.season.season = :seasonId")
    List<Boot> findBySeasonId(@Param("seasonId") Integer seasonId);

        @Query("SELECT b FROM Boot b WHERE b.episode.season.season = :seasonId AND b.episode.episodeNumber <= :episodeNumber")
        List<Boot> findBySeasonIdAndEpisodeNumberLessThanEqual(
            @Param("seasonId") Integer seasonId,
            @Param("episodeNumber") Integer episodeNumber
        );

        @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END " +
            "FROM Boot b " +
            "WHERE b.episode.season.season = :seasonId " +
            "AND b.episode.episodeNumber <= :episodeNumber " +
            "AND b.castaway.id = :castawayPerformanceId")
        boolean existsBySeasonIdAndEpisodeNumberLessThanEqualAndCastawayPerformanceId(
            @Param("seasonId") Integer seasonId,
            @Param("episodeNumber") Integer episodeNumber,
            @Param("castawayPerformanceId") Integer castawayPerformanceId
        );
}
