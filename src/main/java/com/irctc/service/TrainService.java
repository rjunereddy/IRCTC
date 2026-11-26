package com.irctc.service;

import com.irctc.model.Station;
import com.irctc.model.Train;
import com.irctc.model.TrainRouteStop;
import com.irctc.pattern.singleton.AuditLogger;
import com.irctc.repository.StationRepository;
import com.irctc.repository.TrainRepository;
import com.irctc.repository.TrainRouteStopRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TrainService {
    
    private final TrainRepository trainRepository;
    private final StationRepository stationRepository;
    private final TrainRouteStopRepository trainRouteStopRepository;
    
    public TrainService(TrainRepository trainRepository, StationRepository stationRepository, TrainRouteStopRepository trainRouteStopRepository) {
        this.trainRepository = trainRepository;
        this.stationRepository = stationRepository;
        this.trainRouteStopRepository = trainRouteStopRepository;
    }
    
    public List<Train> getAllTrains() {
        return trainRepository.findByIsActiveTrue();
    }
    
    public Train getTrainByNumber(String trainNo) {
        if (trainNo == null || trainNo.trim().isEmpty()) {
            throw new RuntimeException("Train number is required");
        }
        return trainRepository.findById(trainNo.trim())
            .orElseThrow(() -> new RuntimeException("Train not found: " + trainNo));
    }
    
    @Transactional
    public Train addTrain(Train train, String sourceStationCode, String destinationStationCode, String intermediateStations) {
        if (train == null) {
            throw new RuntimeException("Train data is required");
        }
        
        if (train.getTrainNo() != null && trainRepository.existsById(train.getTrainNo().trim())) {
            throw new RuntimeException("Train with this number already exists");
        }
        
        if (sourceStationCode == null || sourceStationCode.trim().isEmpty()) {
            throw new RuntimeException("Source station code is required");
        }
        if (destinationStationCode == null || destinationStationCode.trim().isEmpty()) {
            throw new RuntimeException("Destination station code is required");
        }
        
        AuditLogger.getInstance().log("ADD_TRAIN", "ADMIN", 
                                      "Train: " + train.getTrainNo() + " - " + train.getTrainName());
        
        Station source = stationRepository.findById(sourceStationCode.trim().toUpperCase())
            .orElseThrow(() -> new RuntimeException("Source station not found: " + sourceStationCode));
        Station destination = stationRepository.findById(destinationStationCode.trim().toUpperCase())
            .orElseThrow(() -> new RuntimeException("Destination station not found: " + destinationStationCode));
        
        train.setTrainNo(train.getTrainNo() != null ? train.getTrainNo().trim() : null);
        train.setSourceStation(source);
        train.setDestinationStation(destination);
        
        if (train.getTotalSeats() != null) {
            train.setAvailableSeats(train.getTotalSeats());
        } else {
            throw new RuntimeException("Total seats must be specified");
        }
        
        Train savedTrain = trainRepository.save(train);
        
        java.util.List<TrainRouteStop> stops = new java.util.ArrayList<>();
        TrainRouteStop srcStop = new TrainRouteStop();
        srcStop.setTrain(savedTrain);
        srcStop.setStation(source);
        srcStop.setStopOrder(0);
        srcStop.setDepartureTime(savedTrain.getDepartureTime());
        srcStop.setDistanceInKm(0);
        stops.add(srcStop);
        
        int stopIndex = 1;
        if (intermediateStations != null && !intermediateStations.trim().isEmpty()) {
            String[] codes = intermediateStations.split(",");
            for (String code : codes) {
                Station inter = stationRepository.findById(code.trim().toUpperCase()).orElse(null);
                if (inter != null) {
                    TrainRouteStop interStop = new TrainRouteStop();
                    interStop.setTrain(savedTrain);
                    interStop.setStation(inter);
                    interStop.setStopOrder(stopIndex);
                    // Approximate times
                    interStop.setArrivalTime(savedTrain.getDepartureTime().plusHours(stopIndex * 2L));
                    interStop.setDepartureTime(savedTrain.getDepartureTime().plusHours(stopIndex * 2L).plusMinutes(10));
                    interStop.setDistanceInKm(stopIndex * 150);
                    stops.add(interStop);
                    stopIndex++;
                }
            }
        }
        
        TrainRouteStop destStop = new TrainRouteStop();
        destStop.setTrain(savedTrain);
        destStop.setStation(destination);
        destStop.setStopOrder(stopIndex);
        destStop.setArrivalTime(savedTrain.getArrivalTime());
        destStop.setDistanceInKm(stopIndex * 150);
        stops.add(destStop);
        
        trainRouteStopRepository.saveAll(stops);
        
        return savedTrain;
    }
    
    @Transactional
    public Train addTrainWithStructuredStops(Train train, String sourceStationCode, String destinationStationCode, List<TrainRouteStop> customStops) {
        if (train == null) {
            throw new RuntimeException("Train data is required");
        }
        if (train.getTrainNo() != null && trainRepository.existsById(train.getTrainNo().trim())) {
            throw new RuntimeException("Train with number " + train.getTrainNo() + " already exists");
        }
        Station source = stationRepository.findById(sourceStationCode.trim().toUpperCase())
            .orElseThrow(() -> new RuntimeException("Source station not found: " + sourceStationCode));
        Station destination = stationRepository.findById(destinationStationCode.trim().toUpperCase())
            .orElseThrow(() -> new RuntimeException("Destination station not found: " + destinationStationCode));
        
        train.setSourceStation(source);
        train.setDestinationStation(destination);
        if (train.getTotalSeats() != null) {
            train.setAvailableSeats(train.getTotalSeats());
        }
        
        Train savedTrain = trainRepository.save(train);
        AuditLogger.getInstance().log("ADD_TRAIN_STRUCTURED", "ADMIN", "Train: " + savedTrain.getTrainNo());
        
        if (customStops != null && !customStops.isEmpty()) {
            for (int i = 0; i < customStops.size(); i++) {
                TrainRouteStop stop = customStops.get(i);
                stop.setTrain(savedTrain);
                stop.setStopOrder(i);
            }
            trainRouteStopRepository.saveAll(customStops);
        }
        return savedTrain;
    }
    
    @Transactional
    @SuppressWarnings("null")
    public Train updateTrain(String trainNo, Train updatedTrain) {
        Train existing = getTrainByNumber(trainNo);
        
        if (updatedTrain.getTrainName() != null) {
            existing.setTrainName(updatedTrain.getTrainName());
        }
        if (updatedTrain.getDepartureTime() != null) {
            existing.setDepartureTime(updatedTrain.getDepartureTime());
        }
        if (updatedTrain.getArrivalTime() != null) {
            existing.setArrivalTime(updatedTrain.getArrivalTime());
        }
        if (updatedTrain.getTotalSeats() != null) {
            existing.setTotalSeats(updatedTrain.getTotalSeats());
        }
        if (updatedTrain.getClasses() != null) {
            existing.setClasses(updatedTrain.getClasses());
        }
        if (updatedTrain.getFareStructure() != null) {
            existing.setFareStructure(updatedTrain.getFareStructure());
        }
        
        AuditLogger.getInstance().log("UPDATE_TRAIN", "ADMIN", "Train: " + trainNo);
        return trainRepository.save(existing);
    }
    
    @Transactional
    public void deleteTrain(String trainNo) {
        Train train = getTrainByNumber(trainNo);
        train.setActive(false);
        trainRepository.save(train);
        
        AuditLogger.getInstance().log("DELETE_TRAIN", "ADMIN", "Train: " + trainNo);
    }
    
    public List<Train> searchTrains(String source, String destination) {
        if (source == null || destination == null || 
            source.trim().isEmpty() || destination.trim().isEmpty()) {
            return List.of();
        }
        return trainRepository.findTrainsByRoute(source, destination);
    }
    
    public List<TrainRouteStop> getTrainStops(String trainNo) {
        return trainRouteStopRepository.findByTrain_TrainNoOrderByStopOrderAsc(trainNo);
    }
    
    public List<TrainRouteStop> getStationStops(String stationCode) {
        return trainRouteStopRepository.findByStation_StationCodeOrderByArrivalTimeAsc(stationCode);
    }
    
    @Transactional
    public TrainRouteStop updateStopPlatform(Long stopId, String platformNumber) {
        TrainRouteStop stop = trainRouteStopRepository.findById(stopId)
            .orElseThrow(() -> new RuntimeException("Stop not found: " + stopId));
        stop.setPlatformNumber(platformNumber);
        return trainRouteStopRepository.save(stop);
    }
    
    @Transactional
    public TrainRouteStop updateStopDelay(Long stopId, Integer delayMinutes) {
        TrainRouteStop stop = trainRouteStopRepository.findById(stopId)
            .orElseThrow(() -> new RuntimeException("Stop not found: " + stopId));
        stop.setDelayMinutes(delayMinutes != null ? delayMinutes : 0);
        return trainRouteStopRepository.save(stop);
    }
}