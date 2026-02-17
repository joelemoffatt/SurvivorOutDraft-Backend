package com.vivida;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class EpisodeService {

    private final EpisodeRepository episodeRepository;

    public EpisodeService(EpisodeRepository episodeRepository) {
        this.episodeRepository = episodeRepository;
    }

    public List<Episode> getAllEpisodes() {
        return episodeRepository.findAll();
    }

    public Episode getEpisodeById(int id) {
        return episodeRepository.findById(id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Episode not found with id " + id
        ));
    }

    public void insertEpisode(Episode episode) {
        episodeRepository.save(episode);
    }

    public void updateEpisode(Episode episode) {
        episodeRepository.save(episode);
    }

    public void deleteEpisodeById(int id) {
        episodeRepository.deleteById(id);
    }
}
