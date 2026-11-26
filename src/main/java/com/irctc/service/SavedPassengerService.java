package com.irctc.service;

import com.irctc.model.Passenger;
import com.irctc.model.SavedPassenger;
import com.irctc.repository.SavedPassengerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SavedPassengerService {

    private final SavedPassengerRepository repository;

    public SavedPassengerService(SavedPassengerRepository repository) {
        this.repository = repository;
    }

    public List<SavedPassenger> getSavedPassengers(Passenger passenger) {
        return repository.findByPassenger(passenger);
    }

    @Transactional
    public SavedPassenger addSavedPassenger(SavedPassenger savedPassenger) {
        return repository.save(savedPassenger);
    }

    @Transactional
    public void deleteSavedPassenger(Long id, Passenger passenger) {
        SavedPassenger sp = repository.findById(id).orElse(null);
        if (sp != null && sp.getPassenger().getUserId().equals(passenger.getUserId())) {
            repository.delete(sp);
        }
    }
}
