package com.vivida;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ChallengeService {

    private final ChallengeRepository challengeRepository;

    public ChallengeService(ChallengeRepository challengeRepository) {
        this.challengeRepository = challengeRepository;
    }

    public List<Challenge> getAllChallenges() {
        return challengeRepository.findAll();
    }

    public Challenge getChallengeById(int id) {
        return challengeRepository.findById(id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Challenge not found with id " + id
        ));
    }

    public List<Challenge> getChallengesBySeasonAndEpisode(Integer seasonId, Integer episodeNumber) {
        return challengeRepository.findBySeasonAndEpisode(seasonId, episodeNumber);
    }

    public List<Challenge> getChallengesByEpisodeId(Integer episodeId) {
        return challengeRepository.findByEpisodeId(episodeId);
    }

    public void insertChallenge(Challenge challenge) {
        challengeRepository.save(challenge);
    }

    public void updateChallenge(Challenge challenge) {
        challengeRepository.save(challenge);
    }

    public void deleteChallengeById(int id) {
        challengeRepository.deleteById(id);
    }
}
