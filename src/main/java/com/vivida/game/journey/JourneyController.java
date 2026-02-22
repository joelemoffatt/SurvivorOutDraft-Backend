package com.vivida.game.journey;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/journeys")
public class JourneyController {

    private final JourneyService journeyService;

    public JourneyController(JourneyService journeyService) {
        this.journeyService = journeyService;
    }

    @GetMapping
    public List<Journey> getJourneys() {
        return journeyService.getAllJourneys();
    }

    @GetMapping("{id}")
    public Journey getJourneyById(@PathVariable Integer id) {
        return journeyService.getJourneyById(id);
    }

    @PostMapping
    public void addJourney(@RequestBody Journey journey) {
        journeyService.insertJourney(journey);
    }

    @PutMapping
    public void updateJourney(@RequestBody Journey journey) {
        journeyService.updateJourney(journey);
    }

    @DeleteMapping("{id}")
    public void deleteJourney(@PathVariable Integer id) {
        journeyService.deleteJourneyById(id);
    }
}
