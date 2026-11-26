package com.irctc.dto;

import com.irctc.model.Train;
import com.irctc.model.enums.ClassType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

public class TrainAvailabilityDTO {
    private Train train;
    private LocalDate date;
    private String sourceStationCode;
    private String destinationStationCode;
    private String sourceStationName;
    private String destinationStationName;
    private LocalTime departureTimeAtSource;
    private LocalTime arrivalTimeAtDestination;
    private Integer distanceKm;
    private Map<ClassType, AvailabilityInfo> classAvailMap = new HashMap<>();
    private Map<ClassType, Double> classFares = new HashMap<>();

    public TrainAvailabilityDTO(Train train, LocalDate date) {
        this.train = train;
        this.date = date;
    }

    public void addClassAvailability(ClassType ct, int totalSeats, int bookedConfirmed, 
                                     int bookedRac, int bookedWl, int racCapacity, int wlCapacity) {
        classAvailMap.put(ct, new AvailabilityInfo(totalSeats, bookedConfirmed, bookedRac, bookedWl, racCapacity, wlCapacity));
    }

    public void addClassFare(ClassType ct, Double fare) {
        classFares.put(ct, fare);
    }

    public Train getTrain() { return train; }
    public LocalDate getDate() { return date; }
    public Map<ClassType, AvailabilityInfo> getClassAvailMap() { return classAvailMap; }
    public Map<ClassType, Double> getClassFares() { return classFares; }

    public String getSourceStationCode() { return sourceStationCode; }
    public void setSourceStationCode(String sourceStationCode) { this.sourceStationCode = sourceStationCode; }

    public String getDestinationStationCode() { return destinationStationCode; }
    public void setDestinationStationCode(String destinationStationCode) { this.destinationStationCode = destinationStationCode; }

    public String getSourceStationName() { return sourceStationName; }
    public void setSourceStationName(String sourceStationName) { this.sourceStationName = sourceStationName; }

    public String getDestinationStationName() { return destinationStationName; }
    public void setDestinationStationName(String destinationStationName) { this.destinationStationName = destinationStationName; }

    public LocalTime getDepartureTimeAtSource() { return departureTimeAtSource; }
    public void setDepartureTimeAtSource(LocalTime departureTimeAtSource) { this.departureTimeAtSource = departureTimeAtSource; }

    public LocalTime getArrivalTimeAtDestination() { return arrivalTimeAtDestination; }
    public void setArrivalTimeAtDestination(LocalTime arrivalTimeAtDestination) { this.arrivalTimeAtDestination = arrivalTimeAtDestination; }

    public Integer getDistanceKm() { return distanceKm; }
    public void setDistanceKm(Integer distanceKm) { this.distanceKm = distanceKm; }

    public static class AvailabilityInfo {
        public String badgeClass;
        public String availabilityLabel;
        public boolean fullyBooked;

        public AvailabilityInfo(int totalSeats, int bookedConfirmed, int bookedRac, int bookedWl, int racCapacity, int wlCapacity) {
            int available = totalSeats - bookedConfirmed;
            if (available > 0) {
                this.badgeClass = "avail-confirmed";
                this.availabilityLabel = "AVAILABLE-" + available;
                this.fullyBooked = false;
            } else {
                int racLeft = racCapacity - bookedRac;
                if (racLeft > 0) {
                    this.badgeClass = "avail-rac";
                    this.availabilityLabel = "RAC " + (bookedRac + 1);
                    this.fullyBooked = false;
                } else {
                    int wlLeft = wlCapacity - bookedWl;
                    if (wlLeft > 0) {
                        this.badgeClass = "avail-wl";
                        this.availabilityLabel = "WL " + (bookedWl + 1);
                        this.fullyBooked = false;
                    } else {
                        this.badgeClass = "avail-regret";
                        this.availabilityLabel = "REGRET";
                        this.fullyBooked = true;
                    }
                }
            }
        }

        public String getBadgeClass() { return badgeClass; }
        public String getAvailabilityLabel() { return availabilityLabel; }
        public boolean isFullyBooked() { return fullyBooked; }
    }
}
