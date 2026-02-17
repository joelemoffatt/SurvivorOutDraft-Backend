package com.vivida;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class JourneyService {

    private final JourneyRepository journeyRepository;

    public JourneyService(JourneyRepository journeyRepository) {
        this.journeyRepository = journeyRepository;
    }

    public List<Journey> getAllJourneys() {
        return journeyRepository.findAll();
    }

    public Journey getJourneyById(int id) {
        return journeyRepository.findById(id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Journey not found with id " + id
        ));
    }

    public void insertJourney(Journey journey) {
        journeyRepository.save(journey);
    }

    public void updateJourney(Journey journey) {
        journeyRepository.save(journey);
    }

    public void deleteJourneyById(int id) {
        journeyRepository.deleteById(id);
    }
}
