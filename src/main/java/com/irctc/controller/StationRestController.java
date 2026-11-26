package com.irctc.controller;

import com.irctc.model.Station;
import com.irctc.repository.StationRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/stations")
public class StationRestController {

    private final StationRepository stationRepository;

    public StationRestController(StationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }

    @GetMapping("/search")
    public List<Station> searchStations(@RequestParam String query) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }
        return stationRepository.findByStationCodeContainingIgnoreCaseOrStationNameContainingIgnoreCase(query, query);
    }
}
