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
    public List<AdvantageMovementDTO> getAdvantageMovements() {
        return advantageMovementService.getAllAdvantageMovements().stream()
                .map(AdvantageMovementDTO::new)
                .toList();
    }

    @GetMapping("{id}")
    public AdvantageMovementDTO getAdvantageMovementById(@PathVariable Integer id) {
        return new AdvantageMovementDTO(advantageMovementService.getAdvantageMovementById(id));
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
