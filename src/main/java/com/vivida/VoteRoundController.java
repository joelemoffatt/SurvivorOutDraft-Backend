package com.vivida;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/vote-rounds")
public class VoteRoundController {

    private final VoteRoundService voteRoundService;

    public VoteRoundController(VoteRoundService voteRoundService) {
        this.voteRoundService = voteRoundService;
    }

    @GetMapping
    public List<VoteRound> getVoteRounds() {
        return voteRoundService.getAllVoteRounds();
    }

    @GetMapping("{id}")
    public VoteRound getVoteRoundById(@PathVariable Integer id) {
        return voteRoundService.getVoteRoundById(id);
    }

    @PostMapping
    public void addVoteRound(@RequestBody VoteRound voteRound) {
        voteRoundService.insertVoteRound(voteRound);
    }

    @PutMapping
    public void updateVoteRound(@RequestBody VoteRound voteRound) {
        voteRoundService.updateVoteRound(voteRound);
    }

    @DeleteMapping("{id}")
    public void deleteVoteRound(@PathVariable Integer id) {
        voteRoundService.deleteVoteRoundById(id);
    }
}
