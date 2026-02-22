package com.vivida.game.castaway;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/v1/castaways")
public class CastawayController {

    private final CastawayService castawayService;
    private final CastawayPerformanceService castawayPerformanceService;

    public CastawayController(CastawayService castawayService, CastawayPerformanceService castawayPerformanceService) {
        this.castawayService = castawayService;
        this.castawayPerformanceService = castawayPerformanceService;
    }

    @GetMapping
    public List<Castaway> getCastaways(@RequestParam(required = false) Integer seasonId) {
        if (seasonId != null) {
            List<CastawayPerformance> performances = castawayPerformanceService.getCastawayPerformancesBySeasonId(seasonId);
            return performances.stream()
                    .map(CastawayPerformance::getCastaway)
                    .collect(Collectors.toList());
        }
        return castawayService.getAllCastaways();
    }

    @GetMapping("{id}")
    public Castaway getCastawayById(@PathVariable Integer id) {
        return castawayService.getCastawayById(id);
    }

    @GetMapping("by-json-id/{jsonId}")
    public Castaway getCastawayByJsonId(@PathVariable String jsonId) {
        return castawayService.getCastawayByJsonId(jsonId);
    }

    @PostMapping
    public void addCastaway(@RequestBody Castaway castaway) {
        castawayService.insertCastaway(castaway);
    }

    @PutMapping
    public void updateCastaway(@RequestBody Castaway castaway) {
        castawayService.updateCastaway(castaway);
    }

    @DeleteMapping
    public void deleteCastaway(@RequestBody Integer id) {
        castawayService.deleteCastawayById(id);
    }
}
