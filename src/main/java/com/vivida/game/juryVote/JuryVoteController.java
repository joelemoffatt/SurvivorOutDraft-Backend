package com.vivida.game.juryVote;

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
    public List<JuryVoteDTO> getJuryVotes() {
        return juryVoteService.getAllJuryVotes().stream()
                .map(JuryVoteDTO::new)
                .toList();
    }

    @GetMapping("{id}")
    public JuryVoteDTO getJuryVoteById(@PathVariable Integer id) {
        return new JuryVoteDTO(juryVoteService.getJuryVoteById(id));
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
