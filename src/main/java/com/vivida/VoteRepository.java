package com.vivida;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Integer> {
    
    @Query("SELECT v FROM Vote v WHERE v.voteRound.tribal.episode.season.season = :seasonId")
    List<Vote> findBySeasonId(@Param("seasonId") Integer seasonId);
}
