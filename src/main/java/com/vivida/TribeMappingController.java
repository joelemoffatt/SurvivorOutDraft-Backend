package com.vivida;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/tribe-mappings")
public class TribeMappingController {

    private final TribeMappingService tribeMappingService;

    public TribeMappingController(TribeMappingService tribeMappingService) {
        this.tribeMappingService = tribeMappingService;
    }

    @GetMapping
    public List<TribeMapping> getTribeMappings() {
        return tribeMappingService.getAllTribeMappings();
    }

    @GetMapping("{id}")
    public TribeMapping getTribeMappingById(@PathVariable Integer id) {
        return tribeMappingService.getTribeMappingById(id);
    }

    @PostMapping
    public void addTribeMapping(@RequestBody TribeMapping tribeMapping) {
        tribeMappingService.insertTribeMapping(tribeMapping);
    }

    @PutMapping
    public void updateTribeMapping(@RequestBody TribeMapping tribeMapping) {
        tribeMappingService.updateTribeMapping(tribeMapping);
    }

    @DeleteMapping("{id}")
    public void deleteTribeMapping(@PathVariable Integer id) {
        tribeMappingService.deleteTribeMappingById(id);
    }
}
