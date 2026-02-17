package com.vivida;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/jury-votes")
public class JuryVoteController {

    private final JuryVoteService juryVoteService;

    public JuryVoteController(JuryVoteService juryVoteService) {
        this.juryVoteService = juryVoteService;
    }

    @GetMapping
    public List<JuryVote> getJuryVotes() {
        return juryVoteService.getAllJuryVotes();
    }

    @GetMapping("{id}")
    public JuryVote getJuryVoteById(@PathVariable Integer id) {
        return juryVoteService.getJuryVoteById(id);
    }

    @PostMapping
    public void addJuryVote(@RequestBody JuryVote juryVote) {
        juryVoteService.insertJuryVote(juryVote);
    }

    @PutMapping
    public void updateJuryVote(@RequestBody JuryVote juryVote) {
        juryVoteService.updateJuryVote(juryVote);
    }

    @DeleteMapping("{id}")
    public void deleteJuryVote(@PathVariable Integer id) {
        juryVoteService.deleteJuryVoteById(id);
    }
}
