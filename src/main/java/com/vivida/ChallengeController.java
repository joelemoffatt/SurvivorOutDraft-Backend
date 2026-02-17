package com.vivida;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/challenges")
public class ChallengeController {

    private final ChallengeService challengeService;

    public ChallengeController(ChallengeService challengeService) {
        this.challengeService = challengeService;
    }

    @GetMapping
    public List<Challenge> getChallenges() {
        return challengeService.getAllChallenges();
    }

    @GetMapping("{id}")
    public Challenge getChallengeById(@PathVariable Integer id) {
        return challengeService.getChallengeById(id);
    }

    @PostMapping
    public void addChallenge(@RequestBody Challenge challenge) {
        challengeService.insertChallenge(challenge);
    }

    @PutMapping
    public void updateChallenge(@RequestBody Challenge challenge) {
        challengeService.updateChallenge(challenge);
    }

    @DeleteMapping("{id}")
    public void deleteChallenge(@PathVariable Integer id) {
        challengeService.deleteChallengeById(id);
    }
}
