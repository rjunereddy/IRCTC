package com.irctc.model;

import jakarta.persistence.*;
import java.time.LocalTime;

@Entity
@Table(name = "train_route_stops")
public class TrainRouteStop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "train_no", nullable = false)
    private Train train;

    @ManyToOne
    @JoinColumn(name = "station_code", nullable = false)
    private Station station;

    @Column(nullable = false)
    private Integer stopOrder;

    private LocalTime arrivalTime;
    private LocalTime departureTime;
    
    private Integer distanceInKm;

    public TrainRouteStop() {}

    public Long getId() { return id; }
    public Train getTrain() { return train; }
    public void setTrain(Train train) { this.train = train; }
    
    public Station getStation() { return station; }
    public void setStation(Station station) { this.station = station; }
    
    public Integer getStopOrder() { return stopOrder; }
    public void setStopOrder(Integer stopOrder) { this.stopOrder = stopOrder; }
    
    public LocalTime getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(LocalTime arrivalTime) { this.arrivalTime = arrivalTime; }
    
    public LocalTime getDepartureTime() { return departureTime; }
    public void setDepartureTime(LocalTime departureTime) { this.departureTime = departureTime; }
    
    public Integer getDistanceInKm() { return distanceInKm; }
    public void setDistanceInKm(Integer distanceInKm) { this.distanceInKm = distanceInKm; }
}
