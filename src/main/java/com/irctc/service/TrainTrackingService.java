package com.irctc.service;

import com.irctc.dto.LiveTrainStatusDTO;
import com.irctc.model.Train;
import com.irctc.model.TrainRouteStop;
import com.irctc.repository.TrainRepository;
import com.irctc.repository.TrainRouteStopRepository;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TrainTrackingService {

    private final TrainRepository trainRepository;
    private final TrainRouteStopRepository trainRouteStopRepository;

    public TrainTrackingService(TrainRepository trainRepository, TrainRouteStopRepository trainRouteStopRepository) {
        this.trainRepository = trainRepository;
        this.trainRouteStopRepository = trainRouteStopRepository;
    }

    public LiveTrainStatusDTO getLiveTracking(String trainNo) {
        Train train = trainRepository.findById(trainNo)
                .orElseThrow(() -> new RuntimeException("Train not found: " + trainNo));

        LiveTrainStatusDTO dto = new LiveTrainStatusDTO();
        dto.setTrainNo(train.getTrainNo());
        dto.setTrainName(train.getTrainName());
        dto.setSourceStation(train.getSourceStation() != null ? train.getSourceStation().getStationName() + " (" + train.getSourceStation().getStationCode() + ")" : "Source");
        dto.setDestinationStation(train.getDestinationStation() != null ? train.getDestinationStation().getStationName() + " (" + train.getDestinationStation().getStationCode() + ")" : "Destination");
        dto.setScheduledDeparture(train.getDepartureTime());
        dto.setScheduledArrival(train.getArrivalTime());

        List<TrainRouteStop> rawStops = trainRouteStopRepository.findByTrain_TrainNoOrderByStopOrderAsc(trainNo);
        List<LiveTrainStatusDTO.StationStopDTO> stopDTOs = new ArrayList<>();

        LocalTime now = LocalTime.now();
        LocalTime dep = train.getDepartureTime() != null ? train.getDepartureTime() : LocalTime.of(8, 0);
        LocalTime arr = train.getArrivalTime() != null ? train.getArrivalTime() : LocalTime.of(20, 0);

        int depSec = dep.toSecondOfDay();
        int arrSec = arr.toSecondOfDay();
        if (arrSec <= depSec) arrSec += 86400; // Overnight train

        int nowSec = now.toSecondOfDay();
        if (nowSec < depSec && (depSec - nowSec > 43200)) {
            nowSec += 86400;
        }

        int progress = 0;
        String statusCategory;
        String badgeText;
        int delayMin = (Integer.parseInt(train.getTrainNo().replaceAll("\\D", "0")) % 17); // Realistic pseudo-random delay

        if (nowSec < depSec) {
            progress = 0;
            statusCategory = "YET_TO_START";
            badgeText = "Yet to Depart (Scheduled: " + dep + ")";
            dto.setSpeedKmph(0);
        } else if (nowSec >= arrSec) {
            progress = 100;
            statusCategory = "COMPLETED";
            badgeText = "Arrived at Destination";
            dto.setSpeedKmph(0);
        } else {
            progress = (int) (((double) (nowSec - depSec) / (arrSec - depSec)) * 100.0);
            progress = Math.min(99, Math.max(5, progress));
            statusCategory = delayMin <= 5 ? "ON_TIME" : "DELAYED";
            badgeText = delayMin <= 5 ? "On Time" : "Running Late by " + delayMin + " mins";
            dto.setSpeedKmph(95 + (progress % 25));
        }

        dto.setProgressPercent(progress);
        dto.setStatusCategory(statusCategory);
        dto.setStatusBadgeText(badgeText);
        dto.setDelayMinutes(delayMin);

        // Process route stops
        if (!rawStops.isEmpty()) {
            int crossedCount = (int) Math.round((progress / 100.0) * rawStops.size());
            for (int i = 0; i < rawStops.size(); i++) {
                TrainRouteStop stop = rawStops.get(i);
                boolean isCrossed = i < crossedCount;
                String stnCode = stop.getStation() != null ? stop.getStation().getStationCode() : "STN";
                String stnName = stop.getStation() != null ? stop.getStation().getStationName() : "Station";
                String arrStr = stop.getArrivalTime() != null ? stop.getArrivalTime().toString() : "--";
                String depStr = stop.getDepartureTime() != null ? stop.getDepartureTime().toString() : "--";
                int dist = stop.getDistanceInKm() != null ? stop.getDistanceInKm() : (i * 120);

                stopDTOs.add(new LiveTrainStatusDTO.StationStopDTO(stnCode, stnName, stop.getStopOrder(), arrStr, depStr, isCrossed, dist));
            }

            int currIdx = Math.max(0, Math.min(rawStops.size() - 1, crossedCount - 1));
            int nextIdx = Math.min(rawStops.size() - 1, currIdx + 1);

            dto.setCurrentStationName(stopDTOs.get(currIdx).getStationName());
            dto.setNextStationName(stopDTOs.get(nextIdx).getStationName());
        } else {
            // Fallback synthetic stops if none in DB
            dto.setCurrentStationName(dto.getSourceStation());
            dto.setNextStationName(dto.getDestinationStation());
        }

        dto.setStops(stopDTOs);
        return dto;
    }
}
