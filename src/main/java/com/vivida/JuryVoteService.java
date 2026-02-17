package com.vivida;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class JuryVoteService {

    private final JuryVoteRepository juryVoteRepository;

    public JuryVoteService(JuryVoteRepository juryVoteRepository) {
        this.juryVoteRepository = juryVoteRepository;
    }

    public List<JuryVote> getAllJuryVotes() {
        return juryVoteRepository.findAll();
    }

    public JuryVote getJuryVoteById(int id) {
        return juryVoteRepository.findById(id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "JuryVote not found with id " + id
        ));
    }

    public void insertJuryVote(JuryVote juryVote) {
        juryVoteRepository.save(juryVote);
    }

    public void updateJuryVote(JuryVote juryVote) {
        juryVoteRepository.save(juryVote);
    }

    public void deleteJuryVoteById(int id) {
        juryVoteRepository.deleteById(id);
    }
}
