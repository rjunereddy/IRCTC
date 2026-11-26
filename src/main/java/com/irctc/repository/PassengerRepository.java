package com.irctc.repository;

import com.irctc.model.Passenger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PassengerRepository extends JpaRepository<Passenger, Long> {
    
    Optional<Passenger> findByUsername(String username);
    boolean existsByUsername(String username);
    
    Optional<Passenger> findByEmail(String email);
    
    @Query("SELECT p FROM Passenger p WHERE p.isSeniorCitizen = true")
    List<Passenger> findAllSeniorCitizens();
    
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.passenger.userId = ?1")
    int countBookingsByPassengerId(Long passengerId);
}