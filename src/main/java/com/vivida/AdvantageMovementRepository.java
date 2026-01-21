package com.vivida;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdvantageMovementRepository extends JpaRepository<AdvantageMovement, Integer> {
}
