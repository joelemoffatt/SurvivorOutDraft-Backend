package com.vivida.game.tribe;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class TribeMappingService {

    private final TribeMappingRepository tribeMappingRepository;

    public TribeMappingService(TribeMappingRepository tribeMappingRepository) {
        this.tribeMappingRepository = tribeMappingRepository;
    }

    public List<TribeMapping> getAllTribeMappings() {
        return tribeMappingRepository.findAll();
    }

    public TribeMapping getTribeMappingById(int id) {
        return tribeMappingRepository.findById(id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "TribeMapping not found with id " + id
        ));
    }

    public void insertTribeMapping(TribeMapping tribeMapping) {
        tribeMappingRepository.save(tribeMapping);
    }

    public void updateTribeMapping(TribeMapping tribeMapping) {
        tribeMappingRepository.save(tribeMapping);
    }

    public void deleteTribeMappingById(int id) {
        tribeMappingRepository.deleteById(id);
    }
}
