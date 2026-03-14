package com.vivida.game.castaway;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface CastawayRepository extends JpaRepository<Castaway, Integer> {
    @Query("SELECT c FROM Castaway c WHERE c.json_id = :jsonId")
    Optional<Castaway> findByJsonId(@Param("jsonId") String jsonId);

    @Query("""
            SELECT new com.vivida.game.castaway.CastawaySearchResultDTO(
                c.id,
                (
                    SELECT MAX(cp.season.season)
                    FROM CastawayPerformance cp
                    WHERE cp.castaway = c
                ),
                c.full_name,
                c.json_id
            )
            FROM Castaway c
            WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(c.full_name) LIKE LOWER(CONCAT('%', :query, '%'))
            ORDER BY c.full_name ASC
            """)
    List<CastawaySearchResultDTO> searchCastawaysWithSeason(@Param("query") String query);
}
