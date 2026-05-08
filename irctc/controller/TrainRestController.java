package com.irctc.controller;

import com.irctc.model.TrainRouteStop;
import com.irctc.service.TrainService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/trains")
public class TrainRestController {

    private final TrainService trainService;

    public TrainRestController(TrainService trainService) {
        this.trainService = trainService;
    }

    @GetMapping("/{trainNo}/schedule")
    @Transactional(readOnly = true)
    public List<TrainRouteStopDto> getTrainSchedule(@PathVariable String trainNo) {
        List<TrainRouteStop> stops = trainService.getTrainStops(trainNo);
        return stops.stream().map(stop -> new TrainRouteStopDto(
                stop.getStation() != null ? stop.getStation().getStationName() : "Unknown",
                stop.getStation() != null ? stop.getStation().getStationCode() : "-",
                stop.getArrivalTime()   != null ? stop.getArrivalTime().toString()   : "Origin",
                stop.getDepartureTime() != null ? stop.getDepartureTime().toString() : "Terminus",
                stop.getDistanceInKm() != null  ? stop.getDistanceInKm() : 0,
                stop.getStopOrder()    != null  ? stop.getStopOrder()    : 0
        )).toList();
    }

    // Flat DTO to avoid recursive JPA entity serialization
    static class TrainRouteStopDto {
        public String stationName;
        public String stationCode;
        public String arrivalTime;
        public String departureTime;
        public int distanceInKm;
        public int stopOrder;

        public TrainRouteStopDto(String stationName, String stationCode,
                                  String arrivalTime, String departureTime,
                                  int distanceInKm, int stopOrder) {
            this.stationName   = stationName;
            this.stationCode   = stationCode;
            this.arrivalTime   = arrivalTime;
            this.departureTime = departureTime;
            this.distanceInKm  = distanceInKm;
            this.stopOrder     = stopOrder;
        }
    }
}
