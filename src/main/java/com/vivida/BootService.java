package com.vivida;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class BootService {

    private final BootRepository bootRepository;

    public BootService(BootRepository bootRepository) {
        this.bootRepository = bootRepository;
    }

    public List<Boot> getAllBoots() {
        return bootRepository.findAll();
    }

    public Boot getBootById(int id) {
        return bootRepository.findById(id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Boot not found with id " + id
        ));
    }

    public void insertBoot(Boot boot) {
        bootRepository.save(boot);
    }

    public void updateBoot(Boot boot) {
        bootRepository.save(boot);
    }

    public void deleteBootById(int id) {
        bootRepository.deleteById(id);
    }
}
