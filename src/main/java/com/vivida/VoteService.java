package com.vivida;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class VoteService {

    private final VoteRepository voteRepository;

    public VoteService(VoteRepository voteRepository) {
        this.voteRepository = voteRepository;
    }

    public List<Vote> getAllVotes() {
        return voteRepository.findAll();
    }

    public Vote getVoteById(int id) {
        return voteRepository.findById(id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Vote not found with id " + id
        ));
    }

    public void insertVote(Vote vote) {
        voteRepository.save(vote);
    }

    public void updateVote(Vote vote) {
        voteRepository.save(vote);
    }

    public void deleteVoteById(int id) {
        voteRepository.deleteById(id);
    }
}
