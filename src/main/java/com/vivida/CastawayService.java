package com.vivida;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CastawayService {

    private final CastawayRepository castawayRepository;

    public CastawayService(CastawayRepository castawayRepository) {
        this.castawayRepository = castawayRepository;
    }

    // Usually map to a Data Transfer Object to not expose sensitive info to project
    // This case all castaways are public
    public List<Castaway> getAllCastaways() {
        return castawayRepository.findAll();
    }
}
