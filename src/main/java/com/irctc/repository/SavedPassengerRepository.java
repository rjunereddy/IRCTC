package com.irctc.repository;

import com.irctc.model.Passenger;
import com.irctc.model.SavedPassenger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SavedPassengerRepository extends JpaRepository<SavedPassenger, Long> {
    List<SavedPassenger> findByPassenger(Passenger passenger);
}
