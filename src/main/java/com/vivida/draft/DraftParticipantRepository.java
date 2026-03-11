package com.vivida.draft;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DraftParticipantRepository extends JpaRepository<DraftParticipant, Integer> {

    List<DraftParticipant> findByDraftIdOrderByDraftPositionAsc(Integer draftId);

    Optional<DraftParticipant> findByDraftIdAndUserId(Integer draftId, Integer userId);

    boolean existsByDraftIdAndUserId(Integer draftId, Integer userId);
}
