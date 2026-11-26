package com.irctc.service;

import com.irctc.model.Station;
import com.irctc.repository.StationRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StationService {

    private final StationRepository stationRepository;

    public StationService(StationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }

    public List<Station> getAllStations() {
        return stationRepository.findAll(Sort.by(Sort.Direction.ASC, "stationName"));
    }

    public Station getStation(String code) {
        if (code == null) return null;
        return stationRepository.findById(code.trim().toUpperCase()).orElse(null);
    }

    @Transactional
    public Station saveStation(Station station) {
        if (station == null || station.getStationCode() == null || station.getStationCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Station code is required");
        }
        station.setStationCode(station.getStationCode().trim().toUpperCase());
        if (station.getPlatformCount() == null || station.getPlatformCount() < 1) {
            station.setPlatformCount(2);
        }
        return stationRepository.save(station);
    }

    @Transactional
    public void deleteStation(String code) {
        if (code != null) {
            stationRepository.deleteById(code.trim().toUpperCase());
        }
    }

    public List<Station> searchStations(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllStations();
        }
        String q = query.trim();
        return stationRepository.findByStationCodeContainingIgnoreCaseOrStationNameContainingIgnoreCase(q, q);
    }
}
