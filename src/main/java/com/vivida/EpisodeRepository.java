package com.vivida;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EpisodeRepository extends JpaRepository<Episode, Integer> {
	Episode findBySeasonAndIsFinaleTrue(Season season);
}
