package com.vivida.game.season;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/seasons")
public class SeasonController {

    private final SeasonService seasonService;

    public SeasonController(SeasonService seasonService) {
        this.seasonService = seasonService;
    }

    @GetMapping
    public List<Season> getSeasons() {
        return seasonService.getAllSeasons();
    }

    @GetMapping("{id}")
    public Season getSeasonById(@PathVariable Integer id) {
        return seasonService.getSeasonById(id);
    }

    @PostMapping
    public void addSeason(@RequestBody Season season) {
        seasonService.insertSeason(season);
    }

    @PutMapping
    public void updateSeason(@RequestBody Season season) {
        seasonService.updateSeason(season);
    }

    @DeleteMapping("{id}")
    public void deleteSeason(@PathVariable Integer id) {
        seasonService.deleteSeasonById(id);
    }
}
