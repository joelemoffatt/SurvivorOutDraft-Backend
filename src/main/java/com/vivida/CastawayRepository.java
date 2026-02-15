package com.vivida;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface CastawayRepository extends JpaRepository<Castaway, Integer> {
    @Query("SELECT c FROM Castaway c WHERE c.json_id = :jsonId")
    Optional<Castaway> findByJsonId(@Param("jsonId") String jsonId);
}
