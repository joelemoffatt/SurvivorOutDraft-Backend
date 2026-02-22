package com.vivida.game.tribal;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/tribals")
public class TribalController {

    private final TribalService tribalService;

    public TribalController(TribalService tribalService) {
        this.tribalService = tribalService;
    }

    @GetMapping
    public List<Tribal> getTribals() {
        return tribalService.getAllTribals();
    }

    @GetMapping("{id}")
    public Tribal getTribalById(@PathVariable Integer id) {
        return tribalService.getTribalById(id);
    }

    @PostMapping
    public void addTribal(@RequestBody Tribal tribal) {
        tribalService.insertTribal(tribal);
    }

    @PutMapping
    public void updateTribal(@RequestBody Tribal tribal) {
        tribalService.updateTribal(tribal);
    }

    @DeleteMapping("{id}")
    public void deleteTribal(@PathVariable Integer id) {
        tribalService.deleteTribalById(id);
    }
}
