package com.vivida;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChallengeRepository extends JpaRepository<Challenge, Integer> {
	
	@Query("SELECT c FROM Challenge c WHERE c.season.season = :seasonId AND c.episode.episodeNumber = :episodeNumber")
	List<Challenge> findBySeasonAndEpisode(@Param("seasonId") Integer seasonId, @Param("episodeNumber") Integer episodeNumber);
	
	@Query("SELECT c FROM Challenge c WHERE c.episode.id = :episodeId")
	List<Challenge> findByEpisodeId(@Param("episodeId") Integer episodeId);
}
