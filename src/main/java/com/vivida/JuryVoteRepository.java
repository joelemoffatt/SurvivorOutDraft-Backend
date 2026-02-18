package com.vivida;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JuryVoteRepository extends JpaRepository<JuryVote, Integer> {
    
    @Query("SELECT jv FROM JuryVote jv WHERE jv.episode.season.season = :seasonId")
    List<JuryVote> findBySeasonId(@Param("seasonId") Integer seasonId);
}
