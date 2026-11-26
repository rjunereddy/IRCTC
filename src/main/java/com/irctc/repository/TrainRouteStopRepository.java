package com.irctc.repository;

import com.irctc.model.TrainRouteStop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainRouteStopRepository extends JpaRepository<TrainRouteStop, Long> {
    List<TrainRouteStop> findByTrain_TrainNoOrderByStopOrderAsc(String trainNo);
    List<TrainRouteStop> findByStation_StationCodeOrderByArrivalTimeAsc(String stationCode);
}
