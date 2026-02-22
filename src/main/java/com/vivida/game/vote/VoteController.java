package com.vivida.game.vote;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/votes")
public class VoteController {

    private final VoteService voteService;

    public VoteController(VoteService voteService) {
        this.voteService = voteService;
    }

    @GetMapping
    public List<Vote> getVotes(
            @RequestParam(required = false) Integer seasonId,
            @RequestParam(required = false) Integer episodeNumber) {
        if (seasonId != null && episodeNumber != null) {
            return voteService.getVotesBySeasonAndEpisode(seasonId, episodeNumber);
        } else if (episodeNumber != null) {
            return voteService.getVotesByEpisodeId(episodeNumber);
        }
        return voteService.getAllVotes();
    }

    @GetMapping("{id}")
    public Vote getVoteById(@PathVariable Integer id) {
        return voteService.getVoteById(id);
    }

    @PostMapping
    public void addVote(@RequestBody Vote vote) {
        voteService.insertVote(vote);
    }

    @PutMapping
    public void updateVote(@RequestBody Vote vote) {
        voteService.updateVote(vote);
    }

    @DeleteMapping("{id}")
    public void deleteVote(@PathVariable Integer id) {
        voteService.deleteVoteById(id);
    }
}
