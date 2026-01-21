package com.vivida;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VoteRoundRepository extends JpaRepository<VoteRound, Integer> {
}
