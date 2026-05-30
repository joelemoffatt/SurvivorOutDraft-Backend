package com.vivida.social.team;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Integer> {
    @EntityGraph(attributePaths = {
            "user",
            "group",
            "group.season",
            "group.latestEpisodeWatched",
            "roster",
            "roster.castawayPerformance",
            "roster.castawayPerformance.season",
            "roster.castawayPerformance.castaway"
    })
    List<Team> findByGroupId(Integer groupId);
    List<Team> findByUserId(Integer userId);

    @EntityGraph(attributePaths = {
            "user",
            "group",
            "group.season",
            "group.latestEpisodeWatched",
            "roster",
            "roster.castawayPerformance",
            "roster.castawayPerformance.season",
            "roster.castawayPerformance.castaway"
    })
    Optional<Team> findByGroupIdAndUserId(Integer groupId, Integer userId);

    boolean existsByGroupIdAndUserId(Integer groupId, Integer userId);
}
