package com.vivida;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class TribeService {

    private final TribeRepository tribeRepository;

    public TribeService(TribeRepository tribeRepository) {
        this.tribeRepository = tribeRepository;
    }

    public List<Tribe> getAllTribes() {
        return tribeRepository.findAll();
    }

    public Tribe getTribeById(int id) {
        return tribeRepository.findById(id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Tribe not found with id " + id
        ));
    }

    public void insertTribe(Tribe tribe) {
        tribeRepository.save(tribe);
    }

    public void updateTribe(Tribe tribe) {
        tribeRepository.save(tribe);
    }

    public void deleteTribeById(int id) {
        tribeRepository.deleteById(id);
    }
}
