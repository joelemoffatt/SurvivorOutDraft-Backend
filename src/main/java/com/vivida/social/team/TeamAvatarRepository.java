package com.vivida.social.team;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeamAvatarRepository extends JpaRepository<TeamAvatar, Integer> {
    Optional<TeamAvatar> findByTeamId(Integer teamId);
    boolean existsByTeamId(Integer teamId);
}
