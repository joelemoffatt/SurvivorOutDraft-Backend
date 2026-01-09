package com.vivida;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
