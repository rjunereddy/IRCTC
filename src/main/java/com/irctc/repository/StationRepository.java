package com.irctc.repository;

import com.irctc.model.Station;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StationRepository extends JpaRepository<Station, String> {
    
    Optional<Station> findByStationCode(String stationCode);
    
    List<Station> findByCity(String city);
    
    List<Station> findByState(String state);
    
    List<Station> findByZone(String zone);
    
    List<Station> findByIsJunctionTrue();
    
    List<Station> findByStationCodeContainingIgnoreCaseOrStationNameContainingIgnoreCase(String code, String name);
}