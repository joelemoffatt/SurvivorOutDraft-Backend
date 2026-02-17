package com.vivida;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AdvantageMovementService {

    private final AdvantageMovementRepository advantageMovementRepository;

    public AdvantageMovementService(AdvantageMovementRepository advantageMovementRepository) {
        this.advantageMovementRepository = advantageMovementRepository;
    }

    public List<AdvantageMovement> getAllAdvantageMovements() {
        return advantageMovementRepository.findAll();
    }

    public AdvantageMovement getAdvantageMovementById(int id) {
        return advantageMovementRepository.findById(id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "AdvantageMovement not found with id " + id
        ));
    }

    public void insertAdvantageMovement(AdvantageMovement advantageMovement) {
        advantageMovementRepository.save(advantageMovement);
    }

    public void updateAdvantageMovement(AdvantageMovement advantageMovement) {
        advantageMovementRepository.save(advantageMovement);
    }

    public void deleteAdvantageMovementById(int id) {
        advantageMovementRepository.deleteById(id);
    }
}
