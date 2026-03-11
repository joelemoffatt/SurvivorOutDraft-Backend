package com.vivida.draft;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DraftRepository extends JpaRepository<Draft, Integer> {

    Optional<Draft> findFirstByGroupIdOrderByCreatedAtDesc(Integer groupId);

    List<Draft> findByGroupId(Integer groupId);

    List<Draft> findByStatus(DraftStatus status);

    @Query("SELECT d FROM Draft d WHERE d.group.id = :groupId AND d.status = :status")
    Optional<Draft> findByGroupIdAndStatus(@Param("groupId") Integer groupId,
                                           @Param("status") DraftStatus status);

    boolean existsByGroupIdAndStatus(Integer groupId, DraftStatus status);
}
