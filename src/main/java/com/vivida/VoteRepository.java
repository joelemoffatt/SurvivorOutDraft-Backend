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
    
    @Query("SELECT v FROM Vote v WHERE v.voteRound.tribal.episode.season.season = :seasonId AND v.voteRound.tribal.episode.episodeNumber = :episodeNumber")
    List<Vote> findBySeasonAndEpisode(@Param("seasonId") Integer seasonId, @Param("episodeNumber") Integer episodeNumber);
    
    @Query("SELECT v FROM Vote v WHERE v.voteRound.tribal.episode.id = :episodeId")
    List<Vote> findByEpisodeId(@Param("episodeId") Integer episodeId);
}
