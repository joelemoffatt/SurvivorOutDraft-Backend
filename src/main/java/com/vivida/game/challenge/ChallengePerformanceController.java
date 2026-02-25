package com.vivida.game.challenge;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/challenge-performances")
public class ChallengePerformanceController {

    private final ChallengePerformanceService challengePerformanceService;

    public ChallengePerformanceController(ChallengePerformanceService challengePerformanceService) {
        this.challengePerformanceService = challengePerformanceService;
    }

    @GetMapping
    public List<ChallengePerformanceDTO> getChallengePerformances() {
        return challengePerformanceService.getAllChallengePerformances().stream()
                .map(ChallengePerformanceDTO::new)
                .toList();
    }

    @GetMapping("{id}")
    public ChallengePerformanceDTO getChallengePerformanceById(@PathVariable Integer id) {
        return new ChallengePerformanceDTO(challengePerformanceService.getChallengePerformanceById(id));
    }

    @PostMapping
    public void addChallengePerformance(@RequestBody ChallengePerformance challengePerformance) {
        challengePerformanceService.insertChallengePerformance(challengePerformance);
    }

    @PutMapping
    public void updateChallengePerformance(@RequestBody ChallengePerformance challengePerformance) {
        challengePerformanceService.updateChallengePerformance(challengePerformance);
    }

    @DeleteMapping("{id}")
    public void deleteChallengePerformance(@PathVariable Integer id) {
        challengePerformanceService.deleteChallengePerformanceById(id);
    }
}
