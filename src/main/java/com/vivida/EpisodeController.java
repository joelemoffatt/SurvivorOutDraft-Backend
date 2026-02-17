package com.vivida;

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
    public List<Episode> getEpisodes() {
        return episodeService.getAllEpisodes();
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
