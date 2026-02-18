package com.vivida;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupRepository extends JpaRepository<Group, Integer> {
    List<Group> findByAdminId(Integer adminId);
    List<Group> findBySeasonId(Integer seasonId);
    List<Group> findByStatus(GroupStatus status);
}
