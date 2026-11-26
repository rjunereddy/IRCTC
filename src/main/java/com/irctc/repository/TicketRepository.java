package com.irctc.repository;

import com.irctc.model.Ticket;
import com.irctc.model.enums.ClassType;
import com.irctc.model.enums.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, String> {
    
    Optional<Ticket> findByPnr(String pnr);
    
    List<Ticket> findByPassenger_UserId(Long userId);
    
    List<Ticket> findByPassenger_UserIdAndStatus(Long userId, TicketStatus status);
    
    List<Ticket> findByTrain_TrainNoAndJourneyDate(String trainNo, LocalDate date);
    
    @Query("SELECT t FROM Ticket t WHERE t.journeyDate = :date AND t.status = :status")
    List<Ticket> findTicketsByDateAndStatus(@Param("date") LocalDate date, 
                                             @Param("status") TicketStatus status);
                                             
    @Query("SELECT t FROM Ticket t WHERE t.journeyDate BETWEEN :startDate AND :endDate AND t.status = :status")
    List<Ticket> findTicketsBetweenDatesAndStatus(@Param("startDate") LocalDate startDate, 
                                                  @Param("endDate") LocalDate endDate,
                                                  @Param("status") TicketStatus status);
    
    // ── Total per-status (all classes combined) ──────────────────────────
    @Query("SELECT COALESCE(SUM(SIZE(t.allPassengers)), 0) FROM Ticket t WHERE t.train.trainNo = :trainNo " +
           "AND t.journeyDate = :date AND t.status = 'CONFIRMED'")
    int countConfirmedPassengerSeats(@Param("trainNo") String trainNo, 
                                     @Param("date") LocalDate date);

    @Query("SELECT COALESCE(SUM(SIZE(t.allPassengers)), 0) FROM Ticket t WHERE t.train.trainNo = :trainNo " +
           "AND t.journeyDate = :date AND t.status = 'WAITING'")
    int countWaitlistedPassengerSeats(@Param("trainNo") String trainNo, 
                                      @Param("date") LocalDate date);

    @Query("SELECT COALESCE(SUM(SIZE(t.allPassengers)), 0) FROM Ticket t WHERE t.train.trainNo = :trainNo " +
           "AND t.journeyDate = :date AND t.status = 'RAC'")
    int countRacPassengerSeats(@Param("trainNo") String trainNo, 
                               @Param("date") LocalDate date);

    // ── Per-class counts (for IRCTC-style per-class availability grid) ───
    @Query("SELECT COALESCE(SUM(SIZE(t.allPassengers)), 0) FROM Ticket t WHERE t.train.trainNo = :trainNo " +
           "AND t.journeyDate = :date AND t.classType = :classType AND t.status = 'CONFIRMED'")
    int countConfirmedByClass(@Param("trainNo") String trainNo,
                              @Param("date") LocalDate date,
                              @Param("classType") ClassType classType);

    @Query("SELECT COALESCE(SUM(SIZE(t.allPassengers)), 0) FROM Ticket t WHERE t.train.trainNo = :trainNo " +
           "AND t.journeyDate = :date AND t.classType = :classType AND t.status = 'RAC'")
    int countRacByClass(@Param("trainNo") String trainNo,
                        @Param("date") LocalDate date,
                        @Param("classType") ClassType classType);

    @Query("SELECT COALESCE(SUM(SIZE(t.allPassengers)), 0) FROM Ticket t WHERE t.train.trainNo = :trainNo " +
           "AND t.journeyDate = :date AND t.classType = :classType AND t.status = 'WAITING'")
    int countWaitingByClass(@Param("trainNo") String trainNo,
                            @Param("date") LocalDate date,
                            @Param("classType") ClassType classType);

    List<Ticket> findByRefundStatus(com.irctc.model.enums.RefundStatus refundStatus);
    
    List<Ticket> findByTrain_TrainNoOrderByJourneyDateDesc(String trainNo);
    
    List<Ticket> findByTrain_TrainNoAndJourneyDateAndStatus(String trainNo, LocalDate date, TicketStatus status);
}