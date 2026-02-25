package com.vivida.game.challenge;

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
    public List<ChallengeDTO> getChallenges(
            @RequestParam(required = false) Integer seasonId,
            @RequestParam(required = false) Integer episodeNumber) {
        if (seasonId != null && episodeNumber != null) {
            return challengeService.getChallengesBySeasonAndEpisode(seasonId, episodeNumber).stream()
                    .map(ChallengeDTO::new)
                    .toList();
        } else if (episodeNumber != null) {
            return challengeService.getChallengesByEpisodeId(episodeNumber).stream()
                    .map(ChallengeDTO::new)
                    .toList();
        }
        return challengeService.getAllChallenges().stream()
                .map(ChallengeDTO::new)
                .toList();
    }

    @GetMapping("{id}")
    public ChallengeDTO getChallengeById(@PathVariable Integer id) {
        return new ChallengeDTO(challengeService.getChallengeById(id));
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
