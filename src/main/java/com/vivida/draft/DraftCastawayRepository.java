package com.vivida.draft;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DraftCastawayRepository extends JpaRepository<DraftCastaway, Integer> {

    List<DraftCastaway> findByDraftIdOrderByIdAsc(Integer draftId);

    Optional<DraftCastaway> findByDraftIdAndCastawayPerformanceId(Integer draftId, Integer castawayPerformanceId);

    boolean existsByDraftIdAndCastawayPerformanceId(Integer draftId, Integer castawayPerformanceId);

    void deleteByDraftId(Integer draftId);
}
