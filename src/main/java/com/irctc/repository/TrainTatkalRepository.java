package com.irctc.repository;

import com.irctc.model.TrainTatkal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TrainTatkalRepository extends JpaRepository<TrainTatkal, Long> {
    
    @org.springframework.data.jpa.repository.Query("SELECT tt FROM TrainTatkal tt WHERE tt.train.trainNo = :trainNo AND tt.journeyDate = :date")
    Optional<TrainTatkal> findByTrain_TrainNoAndJourneyDate(@org.springframework.data.repository.query.Param("trainNo") String trainNo, 
                                                           @org.springframework.data.repository.query.Param("date") LocalDate journeyDate);
    
    boolean existsByTrain_TrainNoAndJourneyDate(String trainNo, LocalDate journeyDate);

    List<TrainTatkal> findByJourneyDate(LocalDate journeyDate);
}
