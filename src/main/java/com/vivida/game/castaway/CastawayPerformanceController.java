package com.vivida.game.castaway;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/castaway-performances")
public class CastawayPerformanceController {

    private final CastawayPerformanceService castawayPerformanceService;

    public CastawayPerformanceController(CastawayPerformanceService castawayPerformanceService) {
        this.castawayPerformanceService = castawayPerformanceService;
    }

    @GetMapping
    public List<CastawayPerformanceDTO> getCastawayPerformances() {
        return castawayPerformanceService.getAllCastawayPerformances().stream()
                .map(CastawayPerformanceDTO::new)
                .toList();
    }

    @GetMapping("{id}")
    public CastawayPerformanceDTO getCastawayPerformanceById(@PathVariable Integer id) {
        return new CastawayPerformanceDTO(castawayPerformanceService.getCastawayPerformanceById(id));
    }

    @PostMapping
    public void addCastawayPerformance(@RequestBody CastawayPerformance castawayPerformance) {
        castawayPerformanceService.insertCastawayPerformance(castawayPerformance);
    }

    @PutMapping
    public void updateCastawayPerformance(@RequestBody CastawayPerformance castawayPerformance) {
        castawayPerformanceService.updateCastawayPerformance(castawayPerformance);
    }

    @DeleteMapping("{id}")
    public void deleteCastawayPerformance(@PathVariable Integer id) {
        castawayPerformanceService.deleteCastawayPerformanceById(id);
    }
}
