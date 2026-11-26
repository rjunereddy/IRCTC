package com.irctc.repository;

import com.irctc.model.Passenger;
import com.irctc.model.PassengerWallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PassengerWalletRepository extends JpaRepository<PassengerWallet, Long> {
    Optional<PassengerWallet> findByPassenger(Passenger passenger);
}
