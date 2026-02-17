package com.vivida;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class SeasonService {

    private final SeasonRepository seasonRepository;

    public SeasonService(SeasonRepository seasonRepository) {
        this.seasonRepository = seasonRepository;
    }

    public List<Season> getAllSeasons() {
        return seasonRepository.findAll();
    }

    public Season getSeasonById(int id) {
        return seasonRepository.findById(id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Season not found with id " + id
        ));
    }

    public void insertSeason(Season season) {
        seasonRepository.save(season);
    }

    public void updateSeason(Season season) {
        seasonRepository.save(season);
    }

    public void deleteSeasonById(int id) {
        seasonRepository.deleteById(id);
    }
}
