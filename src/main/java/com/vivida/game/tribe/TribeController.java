package com.vivida.game.tribe;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/tribes")
public class TribeController {

    private final TribeService tribeService;

    public TribeController(TribeService tribeService) {
        this.tribeService = tribeService;
    }

    @GetMapping
    public List<TribeDTO> getTribes() {
        return tribeService.getAllTribes().stream()
                .map(TribeDTO::new)
                .toList();
    }

    @GetMapping("{id}")
    public TribeDTO getTribeById(@PathVariable Integer id) {
        return new TribeDTO(tribeService.getTribeById(id));
    }

    @PostMapping
    public void addTribe(@RequestBody Tribe tribe) {
        tribeService.insertTribe(tribe);
    }

    @PutMapping
    public void updateTribe(@RequestBody Tribe tribe) {
        tribeService.updateTribe(tribe);
    }

    @DeleteMapping("{id}")
    public void deleteTribe(@PathVariable Integer id) {
        tribeService.deleteTribeById(id);
    }
}
