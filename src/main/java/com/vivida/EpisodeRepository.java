package com.vivida;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EpisodeRepository extends JpaRepository<Episode, Integer> {
	Episode findBySeasonAndIsFinaleTrue(Season season);
	
	@Query("SELECT e FROM Episode e WHERE e.season.season = :seasonId ORDER BY e.episodeNumber ASC")
	List<Episode> findBySeasonId(@Param("seasonId") Integer seasonId);
	
	@Query("SELECT e FROM Episode e WHERE e.season.season = :seasonId AND e.episodeNumber = :episodeNumber")
	Optional<Episode> findBySeasonAndEpisodeNumber(@Param("seasonId") Integer seasonId, @Param("episodeNumber") Integer episodeNumber);

	@EntityGraph(attributePaths = {
			"challenges",
			"challenges.challengesPerformances",
			"challenges.challengesPerformances.castaway",
			"challenges.challengesPerformances.castaway.castaway",
			"journeys",
			"journeys.castaway",
			"journeys.castaway.castaway",
			"advantageMovements",
			"advantageMovements.castawayId",
			"advantageMovements.castawayId.castaway",
			"advantageMovements.playedForId",
			"advantageMovements.playedForId.castaway",
			"tribals",
			"tribals.tribe",
			"tribals.boot",
			"tribals.boot.castaway",
			"tribals.boot.castaway.castaway",
			"tribals.votes",
			"tribals.votes.votes",
			"tribals.votes.votes.castaway",
			"tribals.votes.votes.castaway.castaway",
			"tribals.votes.votes.votedFor",
			"tribals.votes.votes.votedFor.castaway",
			"boots",
			"boots.castaway",
			"boots.castaway.castaway",
			"boots.tribal",
			"boots.tribal.tribe"
	})
	@Query("SELECT e FROM Episode e WHERE e.season.season = :seasonId AND e.episodeNumber = :episodeNumber")
	Optional<Episode> findEpisodeDetailBySeasonAndEpisodeNumber(
			@Param("seasonId") Integer seasonId,
			@Param("episodeNumber") Integer episodeNumber
	);
}
