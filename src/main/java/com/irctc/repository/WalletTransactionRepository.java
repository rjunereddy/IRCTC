package com.irctc.repository;

import com.irctc.model.PassengerWallet;
import com.irctc.model.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {
    List<WalletTransaction> findByWalletOrderByTransactionDateDesc(PassengerWallet wallet);
}
