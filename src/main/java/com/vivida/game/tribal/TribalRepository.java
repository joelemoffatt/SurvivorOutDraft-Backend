package com.vivida.game.tribal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TribalRepository extends JpaRepository<Tribal, Integer> {
}
