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
    public List<EpisodeDTO> getEpisodes(@RequestParam(required = false) Integer seasonId) {
        if (seasonId != null) {
            return episodeService.getEpisodesBySeasonId(seasonId).stream()
                    .map(EpisodeDTO::new)
                    .toList();
        }
        return episodeService.getAllEpisodes().stream()
                .map(EpisodeDTO::new)
                .toList();
    }

    @GetMapping("detail")
    public EpisodeDetailDto getEpisodeDetail(
            @RequestParam Integer seasonId,
            @RequestParam Integer episodeNumber
    ) {
        return episodeService.getEpisodeDetail(seasonId, episodeNumber);
    }

    @GetMapping("{id}")
    public EpisodeDTO getEpisodeById(@PathVariable Integer id) {
        return new EpisodeDTO(episodeService.getEpisodeById(id));
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
