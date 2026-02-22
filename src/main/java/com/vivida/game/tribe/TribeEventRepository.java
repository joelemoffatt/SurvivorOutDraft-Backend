package com.vivida.game.tribe;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TribeEventRepository extends JpaRepository<TribeMapping, Integer> {
}
