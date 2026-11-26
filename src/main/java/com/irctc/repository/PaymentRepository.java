package com.irctc.repository;

import com.irctc.model.Payment;
import com.irctc.model.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {
    
    Optional<Payment> findByTicket_Pnr(String pnr);
    
    List<Payment> findByStatus(PaymentStatus status);
    
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'SUCCESS' " +
           "AND p.paymentDate BETWEEN :start AND :end")
    Double getTotalRevenue(@Param("start") LocalDateTime start, 
                           @Param("end") LocalDateTime end);
}