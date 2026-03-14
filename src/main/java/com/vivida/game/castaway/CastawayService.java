package com.vivida.game.castaway;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
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

    public Castaway getCastawayById(int id) {
        return castawayRepository.findById(id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Castaway not found with id " + id
        ));
    }

    public Castaway getCastawayByJsonId(String jsonId) {
        return castawayRepository.findByJsonId(jsonId).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Castaway not found with json_id " + jsonId
        ));
    }

    public List<CastawaySearchResultDTO> searchCastawaysWithSeason(String query) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }

        return castawayRepository.searchCastawaysWithSeason(query.trim());
    }

    public void insertCastaway(Castaway castaway) {
        castawayRepository.save(castaway);
    }

    public void updateCastaway(Castaway castaway) {
        castawayRepository.save(castaway);
    }

    public void  deleteCastawayById(int id) {
        castawayRepository.deleteById(id);
    }
}
