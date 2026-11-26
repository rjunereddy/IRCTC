package com.irctc.dto;

import java.time.LocalTime;
import java.util.List;

public class LiveTrainStatusDTO {
    private String trainNo;
    private String trainName;
    private String sourceStation;
    private String destinationStation;
    private LocalTime scheduledDeparture;
    private LocalTime scheduledArrival;
    private int progressPercent;
    private String statusCategory; // ON_TIME, DELAYED, COMPLETED, YET_TO_START
    private String statusBadgeText;
    private String currentStationName;
    private String nextStationName;
    private int speedKmph;
    private int delayMinutes;
    private List<StationStopDTO> stops;

    public LiveTrainStatusDTO() {}

    public String getTrainNo() { return trainNo; }
    public void setTrainNo(String trainNo) { this.trainNo = trainNo; }

    public String getTrainName() { return trainName; }
    public void setTrainName(String trainName) { this.trainName = trainName; }

    public String getSourceStation() { return sourceStation; }
    public void setSourceStation(String sourceStation) { this.sourceStation = sourceStation; }

    public String getDestinationStation() { return destinationStation; }
    public void setDestinationStation(String destinationStation) { this.destinationStation = destinationStation; }

    public LocalTime getScheduledDeparture() { return scheduledDeparture; }
    public void setScheduledDeparture(LocalTime scheduledDeparture) { this.scheduledDeparture = scheduledDeparture; }

    public LocalTime getScheduledArrival() { return scheduledArrival; }
    public void setScheduledArrival(LocalTime scheduledArrival) { this.scheduledArrival = scheduledArrival; }

    public int getProgressPercent() { return progressPercent; }
    public void setProgressPercent(int progressPercent) { this.progressPercent = progressPercent; }

    public String getStatusCategory() { return statusCategory; }
    public void setStatusCategory(String statusCategory) { this.statusCategory = statusCategory; }

    public String getStatusBadgeText() { return statusBadgeText; }
    public void setStatusBadgeText(String statusBadgeText) { this.statusBadgeText = statusBadgeText; }

    public String getCurrentStationName() { return currentStationName; }
    public void setCurrentStationName(String currentStationName) { this.currentStationName = currentStationName; }

    public String getNextStationName() { return nextStationName; }
    public void setNextStationName(String nextStationName) { this.nextStationName = nextStationName; }

    public int getSpeedKmph() { return speedKmph; }
    public void setSpeedKmph(int speedKmph) { this.speedKmph = speedKmph; }

    public int getDelayMinutes() { return delayMinutes; }
    public void setDelayMinutes(int delayMinutes) { this.delayMinutes = delayMinutes; }

    public List<StationStopDTO> getStops() { return stops; }
    public void setStops(List<StationStopDTO> stops) { this.stops = stops; }

    public static class StationStopDTO {
        private String stationCode;
        private String stationName;
        private int stopOrder;
        private String arrivalTime;
        private String departureTime;
        private boolean crossed;
        private int distanceKm;

        public StationStopDTO() {}

        public StationStopDTO(String stationCode, String stationName, int stopOrder,
                              String arrivalTime, String departureTime, boolean crossed, int distanceKm) {
            this.stationCode = stationCode;
            this.stationName = stationName;
            this.stopOrder = stopOrder;
            this.arrivalTime = arrivalTime;
            this.departureTime = departureTime;
            this.crossed = crossed;
            this.distanceKm = distanceKm;
        }

        public String getStationCode() { return stationCode; }
        public String getStationName() { return stationName; }
        public int getStopOrder() { return stopOrder; }
        public String getArrivalTime() { return arrivalTime; }
        public String getDepartureTime() { return departureTime; }
        public boolean isCrossed() { return crossed; }
        public int getDistanceKm() { return distanceKm; }
    }
}
