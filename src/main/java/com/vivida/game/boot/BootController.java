package com.vivida.game.boot;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/boots")
public class BootController {

    private final BootService bootService;

    public BootController(BootService bootService) {
        this.bootService = bootService;
    }

    @GetMapping
    public List<Boot> getBoots() {
        return bootService.getAllBoots();
    }

    @GetMapping("{id}")
    public Boot getBootById(@PathVariable Integer id) {
        return bootService.getBootById(id);
    }

    @PostMapping
    public void addBoot(@RequestBody Boot boot) {
        bootService.insertBoot(boot);
    }

    @PutMapping
    public void updateBoot(@RequestBody Boot boot) {
        bootService.updateBoot(boot);
    }

    @DeleteMapping("{id}")
    public void deleteBoot(@PathVariable Integer id) {
        bootService.deleteBootById(id);
    }
}
