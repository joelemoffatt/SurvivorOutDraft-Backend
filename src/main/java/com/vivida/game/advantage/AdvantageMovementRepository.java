package com.vivida.game.advantage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdvantageMovementRepository extends JpaRepository<AdvantageMovement, Integer> {
    
    @Query("SELECT am FROM AdvantageMovement am WHERE am.episode.season.season = :seasonId")
    List<AdvantageMovement> findBySeasonId(@Param("seasonId") Integer seasonId);
}
