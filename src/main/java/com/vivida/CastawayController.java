package com.vivida;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/castaways")
public class CastawayController {

    private final CastawayService  castawayService;

    public CastawayController(CastawayService castawayService) {
        this.castawayService = castawayService;
    }

    @GetMapping
    public List<Castaway> getCastaways() {
        return castawayService.getAllCastaways();
    }

    @GetMapping("{id}")
    public Castaway getCastawayById(@PathVariable Integer id) {
        return castawayService.getCastawayById(id);
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
