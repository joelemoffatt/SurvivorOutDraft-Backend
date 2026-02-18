package com.vivida;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Integer> {
    List<Team> findByGroupId(Integer groupId);
    List<Team> findByUserId(Integer userId);
    Optional<Team> findByGroupIdAndUserId(Integer groupId, Integer userId);
    boolean existsByGroupIdAndUserId(Integer groupId, Integer userId);
}
