package com.irctc.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "stations")
public class Station {
    
    @Id
    @Column(length = 10)
    private String stationCode;
    
    @Column(nullable = false, length = 100)
    private String stationName;
    
    @Column(nullable = false, length = 50)
    private String city;
    
    @Column(nullable = false, length = 50)
    private String state;
    
    @Column(length = 6)
    private String pincode;
    
    private String zone;
    
    @Column(name = "platform_count")
    private Integer platformCount = 2;
    
    @ElementCollection
    @CollectionTable(name = "station_facilities")
    @Column(name = "facility")
    private List<String> facilities = new ArrayList<>();
    
    @Column(name = "is_junction")
    private boolean isJunction = false;
    
    // Default constructor
    public Station() {}
    
    // Parameterized constructor
    public Station(String stationCode, String stationName, String city, String state) {
        this.stationCode = stationCode;
        this.stationName = stationName;
        this.city = city;
        this.state = state;
    }
    
    // Getters and Setters
    public String getStationCode() {
        return stationCode;
    }
    
    public void setStationCode(String stationCode) {
        this.stationCode = stationCode;
    }
    
    public String getStationName() {
        return stationName;
    }
    
    public void setStationName(String stationName) {
        this.stationName = stationName;
    }
    
    public String getCity() {
        return city;
    }
    
    public void setCity(String city) {
        this.city = city;
    }
    
    public String getState() {
        return state;
    }
    
    public void setState(String state) {
        this.state = state;
    }
    
    public String getPincode() {
        return pincode;
    }
    
    public void setPincode(String pincode) {
        this.pincode = pincode;
    }
    
    public String getZone() {
        return zone;
    }
    
    public void setZone(String zone) {
        this.zone = zone;
    }
    
    public Integer getPlatformCount() {
        return platformCount;
    }
    
    public void setPlatformCount(Integer platformCount) {
        this.platformCount = platformCount;
    }
    
    public List<String> getFacilities() {
        return facilities;
    }
    
    public void setFacilities(List<String> facilities) {
        this.facilities = facilities;
    }
    
    public boolean isJunction() {
        return isJunction;
    }
    
    public void setJunction(boolean junction) {
        isJunction = junction;
    }
}