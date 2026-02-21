package com.vivida;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CastawayPerformanceService {

    private final CastawayPerformanceRepository castawayPerformanceRepository;

    public CastawayPerformanceService(CastawayPerformanceRepository castawayPerformanceRepository) {
        this.castawayPerformanceRepository = castawayPerformanceRepository;
    }

    public List<CastawayPerformance> getAllCastawayPerformances() {
        return castawayPerformanceRepository.findAll();
    }

    public CastawayPerformance getCastawayPerformanceById(int id) {
        return castawayPerformanceRepository.findById(id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "CastawayPerformance not found with id " + id
        ));
    }

    public List<CastawayPerformance> getCastawayPerformancesBySeasonId(Integer seasonId) {
        return castawayPerformanceRepository.findBySeasonId(seasonId);
    }

    public void insertCastawayPerformance(CastawayPerformance castawayPerformance) {
        castawayPerformanceRepository.save(castawayPerformance);
    }

    public void updateCastawayPerformance(CastawayPerformance castawayPerformance) {
        castawayPerformanceRepository.save(castawayPerformance);
    }

    public void deleteCastawayPerformanceById(int id) {
        castawayPerformanceRepository.deleteById(id);
    }
}
