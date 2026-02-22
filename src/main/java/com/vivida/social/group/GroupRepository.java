package com.vivida.social.group;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupRepository extends JpaRepository<Group, Integer> {
    List<Group> findByAdminId(Integer adminId);
    List<Group> findBySeasonSeason(Integer season);
    List<Group> findByStatus(GroupStatus status);
    Optional<Group> findByName(String name);
}
