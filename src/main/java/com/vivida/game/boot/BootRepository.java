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
}
