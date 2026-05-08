package com.irctc.model;

import com.irctc.model.enums.ClassType;
import com.irctc.model.enums.QuotaType;
import jakarta.persistence.*;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "trains")
public class Train {
    
    @Id
    @Column(length = 10)
    private String trainNo;
    
    @Column(nullable = false, length = 100)
    private String trainName;
    
    @ManyToOne
    @JoinColumn(name = "source_code")
    private Station sourceStation;
    
    @ManyToOne
    @JoinColumn(name = "destination_code")
    private Station destinationStation;
    
    private LocalTime departureTime;
    private LocalTime arrivalTime;
    
    @Column(name = "total_seats")
    private Integer totalSeats;
    
    @Column(name = "available_seats")
    private Integer availableSeats;
    
    @Column(name = "waitlist_capacity")
    private Integer waitlistCapacity = 50;
    
    @Column(name = "rac_capacity")
    private Integer racCapacity = 20;
    
    @ElementCollection
    @CollectionTable(name = "train_running_days")
    @Column(name = "running_day")
    private String[] runningDays;
    
    @Enumerated(EnumType.STRING)
    private ClassType trainType;
    
    @ElementCollection
    @CollectionTable(name = "train_classes")
    @Column(name = "class_type")
    @Enumerated(EnumType.STRING)
    private List<ClassType> classes;
    
    @ElementCollection
    @CollectionTable(name = "train_fares")
    @MapKeyColumn(name = "class_type")
    @Column(name = "fare")
    private Map<String, Double> fareStructure = new HashMap<>();
    
    @ElementCollection
    @CollectionTable(name = "train_quota_allocation")
    @MapKeyColumn(name = "quota_type")
    @Column(name = "seats")
    private Map<String, Integer> quotaAllocation = new HashMap<>();
    
    @Column(name = "is_active")
    private boolean isActive = true;
    
    // Default constructor
    public Train() {}
    
    // Parameterized constructor
    public Train(String trainNo, String trainName, Station source, Station destination,
                 LocalTime departure, LocalTime arrival) {
        this.trainNo = trainNo;
        this.trainName = trainName;
        this.sourceStation = source;
        this.destinationStation = destination;
        this.departureTime = departure;
        this.arrivalTime = arrival;
    }
    
    // Getters and Setters
    public String getTrainNo() {
        return trainNo;
    }
    
    public void setTrainNo(String trainNo) {
        this.trainNo = trainNo;
    }
    
    public String getTrainName() {
        return trainName;
    }
    
    public void setTrainName(String trainName) {
        this.trainName = trainName;
    }
    
    public Station getSourceStation() {
        return sourceStation;
    }
    
    public void setSourceStation(Station sourceStation) {
        this.sourceStation = sourceStation;
    }
    
    public Station getDestinationStation() {
        return destinationStation;
    }
    
    public void setDestinationStation(Station destinationStation) {
        this.destinationStation = destinationStation;
    }
    
    public LocalTime getDepartureTime() {
        return departureTime;
    }
    
    public void setDepartureTime(LocalTime departureTime) {
        this.departureTime = departureTime;
    }
    
    public LocalTime getArrivalTime() {
        return arrivalTime;
    }
    
    public void setArrivalTime(LocalTime arrivalTime) {
        this.arrivalTime = arrivalTime;
    }
    
    public Integer getTotalSeats() {
        return totalSeats;
    }
    
    public void setTotalSeats(Integer totalSeats) {
        this.totalSeats = totalSeats;
    }
    
    public Integer getAvailableSeats() {
        return availableSeats;
    }
    
    public void setAvailableSeats(Integer availableSeats) {
        this.availableSeats = availableSeats;
    }
    
    public Integer getWaitlistCapacity() {
        return waitlistCapacity != null ? waitlistCapacity : 50;
    }
    
    public void setWaitlistCapacity(Integer waitlistCapacity) {
        this.waitlistCapacity = waitlistCapacity;
    }
    
    public Integer getRacCapacity() {
        return racCapacity != null ? racCapacity : 20;
    }
    
    public void setRacCapacity(Integer racCapacity) {
        this.racCapacity = racCapacity;
    }
    
    public String[] getRunningDays() {
        return runningDays;
    }
    
    public void setRunningDays(String[] runningDays) {
        this.runningDays = runningDays;
    }
    
    public ClassType getTrainType() {
        return trainType;
    }
    
    public void setTrainType(ClassType trainType) {
        this.trainType = trainType;
    }
    
    public List<ClassType> getClasses() {
        return classes;
    }
    
    public void setClasses(List<ClassType> classes) {
        this.classes = classes;
    }
    
    public Map<String, Double> getFareStructure() {
        return fareStructure;
    }
    
    public void setFareStructure(Map<String, Double> fareStructure) {
        this.fareStructure = fareStructure;
    }
    
    public Map<String, Integer> getQuotaAllocation() {
        return quotaAllocation;
    }
    
    public void setQuotaAllocation(Map<String, Integer> quotaAllocation) {
        this.quotaAllocation = quotaAllocation;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
    
    // Business methods
    public boolean checkAvailability() {
        return availableSeats != null && availableSeats > 0;
    }
    
    public double getFare(ClassType classType, QuotaType quotaType) {
        Double baseFare = fareStructure != null ? fareStructure.getOrDefault(classType.name(), 100.0) : 100.0;
        
        if (QuotaType.TATKAL.equals(quotaType)) {
            return baseFare * 1.3;
        } else if (QuotaType.SENIOR_CITIZEN.equals(quotaType)) {
            return baseFare * 0.5;
        } else {
            return baseFare;
        }
    }
}