package com.vivida.draft;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DraftPickRepository extends JpaRepository<DraftPick, Integer> {

    List<DraftPick> findByDraftIdOrderByPickNumberAsc(Integer draftId);

    Optional<DraftPick> findByDraftIdAndPickNumber(Integer draftId, Integer pickNumber);

    List<DraftPick> findByDraftIdAndTeamId(Integer draftId, Integer teamId);

    long countByDraftIdAndCastawayPerformanceIsNotNull(Integer draftId);

    @Query("SELECT dp FROM DraftPick dp WHERE dp.draft.id = :draftId AND dp.castawayPerformance IS NULL ORDER BY dp.pickNumber ASC")
    List<DraftPick> findUnfilledPicksByDraftId(@Param("draftId") Integer draftId);

    boolean existsByDraftIdAndCastawayPerformanceId(Integer draftId, Integer castawayPerformanceId);

    /** How many times a castaway has been drafted across all teams in this draft. */
    long countByDraftIdAndCastawayPerformanceId(Integer draftId, Integer castawayPerformanceId);

    /** Whether a specific team has already drafted this castaway (same-team dup guard). */
    boolean existsByDraftIdAndTeamIdAndCastawayPerformanceId(Integer draftId, Integer teamId, Integer castawayPerformanceId);
}
