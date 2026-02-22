package com.vivida.game.advantage;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/advantage-movements")
public class AdvantageMovementController {

    private final AdvantageMovementService advantageMovementService;

    public AdvantageMovementController(AdvantageMovementService advantageMovementService) {
        this.advantageMovementService = advantageMovementService;
    }

    @GetMapping
    public List<AdvantageMovement> getAdvantageMovements() {
        return advantageMovementService.getAllAdvantageMovements();
    }

    @GetMapping("{id}")
    public AdvantageMovement getAdvantageMovementById(@PathVariable Integer id) {
        return advantageMovementService.getAdvantageMovementById(id);
    }

    @PostMapping
    public void addAdvantageMovement(@RequestBody AdvantageMovement advantageMovement) {
        advantageMovementService.insertAdvantageMovement(advantageMovement);
    }

    @PutMapping
    public void updateAdvantageMovement(@RequestBody AdvantageMovement advantageMovement) {
        advantageMovementService.updateAdvantageMovement(advantageMovement);
    }

    @DeleteMapping("{id}")
    public void deleteAdvantageMovement(@PathVariable Integer id) {
        advantageMovementService.deleteAdvantageMovementById(id);
    }
}
