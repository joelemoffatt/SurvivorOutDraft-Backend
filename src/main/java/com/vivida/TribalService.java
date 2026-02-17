package com.vivida;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class TribalService {

    private final TribalRepository tribalRepository;

    public TribalService(TribalRepository tribalRepository) {
        this.tribalRepository = tribalRepository;
    }

    public List<Tribal> getAllTribals() {
        return tribalRepository.findAll();
    }

    public Tribal getTribalById(int id) {
        return tribalRepository.findById(id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Tribal not found with id " + id
        ));
    }

    public void insertTribal(Tribal tribal) {
        tribalRepository.save(tribal);
    }

    public void updateTribal(Tribal tribal) {
        tribalRepository.save(tribal);
    }

    public void deleteTribalById(int id) {
        tribalRepository.deleteById(id);
    }
}
