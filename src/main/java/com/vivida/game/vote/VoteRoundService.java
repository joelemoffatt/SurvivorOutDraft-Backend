package com.vivida.game.vote;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class VoteRoundService {

    private final VoteRoundRepository voteRoundRepository;

    public VoteRoundService(VoteRoundRepository voteRoundRepository) {
        this.voteRoundRepository = voteRoundRepository;
    }

    public List<VoteRound> getAllVoteRounds() {
        return voteRoundRepository.findAll();
    }

    public VoteRound getVoteRoundById(int id) {
        return voteRoundRepository.findById(id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "VoteRound not found with id " + id
        ));
    }

    public void insertVoteRound(VoteRound voteRound) {
        voteRoundRepository.save(voteRound);
    }

    public void updateVoteRound(VoteRound voteRound) {
        voteRoundRepository.save(voteRound);
    }

    public void deleteVoteRoundById(int id) {
        voteRoundRepository.deleteById(id);
    }
}
