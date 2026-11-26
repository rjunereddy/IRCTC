package com.irctc.repository;

import com.irctc.model.Train;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TrainRepository extends JpaRepository<Train, String> {
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Train t WHERE t.trainNo = :trainNo")
    Optional<Train> findByIdWithLock(@Param("trainNo") String trainNo);
    
    List<Train> findBySourceStation_StationCodeAndDestinationStation_StationCode(
        String sourceCode, String destinationCode);
    
    @Query("SELECT t FROM Train t " +
           "JOIN TrainRouteStop rs1 ON t.trainNo = rs1.train.trainNo " +
           "JOIN TrainRouteStop rs2 ON t.trainNo = rs2.train.trainNo " +
           "WHERE rs1.station.stationCode = :source " +
           "AND rs2.station.stationCode = :dest " +
           "AND rs1.stopOrder < rs2.stopOrder " +
           "AND t.isActive = true")
    List<Train> findTrainsByRoute(@Param("source") String source, 
                                   @Param("dest") String destination);
    
    @Query("SELECT t FROM Train t WHERE t.departureTime BETWEEN :start AND :end")
    List<Train> findTrainsByDepartureTime(@Param("start") LocalTime start, 
                                          @Param("end") LocalTime end);
    
    List<Train> findByTrainNameContainingIgnoreCase(String trainName);
    
    List<Train> findByIsActiveTrue();

    @Query("SELECT t.trainNo, t.sourceStation.stationCode, t.destinationStation.stationCode, t.departureTime, t.arrivalTime FROM Train t")
    List<Object[]> findTrainRouteBasicInfo();
}