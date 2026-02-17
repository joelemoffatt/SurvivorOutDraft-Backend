package com.vivida;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ChallengePerformanceService {

    private final ChallengePerformanceRepository challengePerformanceRepository;

    public ChallengePerformanceService(ChallengePerformanceRepository challengePerformanceRepository) {
        this.challengePerformanceRepository = challengePerformanceRepository;
    }

    public List<ChallengePerformance> getAllChallengePerformances() {
        return challengePerformanceRepository.findAll();
    }

    public ChallengePerformance getChallengePerformanceById(int id) {
        return challengePerformanceRepository.findById(id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "ChallengePerformance not found with id " + id
        ));
    }

    public void insertChallengePerformance(ChallengePerformance challengePerformance) {
        challengePerformanceRepository.save(challengePerformance);
    }

    public void updateChallengePerformance(ChallengePerformance challengePerformance) {
        challengePerformanceRepository.save(challengePerformance);
    }

    public void deleteChallengePerformanceById(int id) {
        challengePerformanceRepository.deleteById(id);
    }
}
