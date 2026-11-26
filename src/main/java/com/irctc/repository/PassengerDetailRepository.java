package com.irctc.repository;

import com.irctc.model.PassengerDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PassengerDetailRepository extends JpaRepository<PassengerDetail, Long> {
    List<PassengerDetail> findByTicket_Pnr(String pnr);
    List<PassengerDetail> findByTicket_Train_TrainNo(String trainNo);
}
