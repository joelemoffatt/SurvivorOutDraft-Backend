package com.vivida.game.episode;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/episodes")
public class EpisodeController {

    private final EpisodeService episodeService;

    public EpisodeController(EpisodeService episodeService) {
        this.episodeService = episodeService;
    }

    @GetMapping
    public List<Episode> getEpisodes(@RequestParam(required = false) Integer seasonId) {
        if (seasonId != null) {
            return episodeService.getEpisodesBySeasonId(seasonId);
        }
        return episodeService.getAllEpisodes();
    }

    @GetMapping("detail")
    public EpisodeDetailDto getEpisodeDetail(
            @RequestParam Integer seasonId,
            @RequestParam Integer episodeNumber
    ) {
        return episodeService.getEpisodeDetail(seasonId, episodeNumber);
    }

    @GetMapping("{id}")
    public Episode getEpisodeById(@PathVariable Integer id) {
        return episodeService.getEpisodeById(id);
    }

    @PostMapping
    public void addEpisode(@RequestBody Episode episode) {
        episodeService.insertEpisode(episode);
    }

    @PutMapping
    public void updateEpisode(@RequestBody Episode episode) {
        episodeService.updateEpisode(episode);
    }

    @DeleteMapping("{id}")
    public void deleteEpisode(@PathVariable Integer id) {
        episodeService.deleteEpisodeById(id);
    }
}
