package com.irctc.service;

import com.irctc.model.Passenger;
import com.irctc.model.PassengerWallet;
import com.irctc.model.WalletTransaction;
import com.irctc.repository.PassengerWalletRepository;
import com.irctc.repository.WalletTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WalletService {

    private final PassengerWalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;

    public WalletService(PassengerWalletRepository walletRepository, WalletTransactionRepository transactionRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    public PassengerWallet getWallet(Passenger passenger) {
        return walletRepository.findByPassenger(passenger)
                .orElseGet(() -> {
                    // New wallet: credit ₹5000 demo balance so users can test immediately
                    PassengerWallet newWallet = new PassengerWallet(passenger);
                    newWallet.setBalance(5000.0);
                    PassengerWallet saved = walletRepository.save(newWallet);
                    WalletTransaction welcome = new WalletTransaction(saved, 5000.0, "CREDIT",
                            "Welcome bonus - Demo wallet credit");
                    transactionRepository.save(welcome);
                    return saved;
                });
    }

    public double getWalletBalance(Passenger passenger) {
        return walletRepository.findByPassenger(passenger)
                .map(PassengerWallet::getBalance)
                .orElse(0.0);
    }

    public List<WalletTransaction> getTransactions(PassengerWallet wallet) {
        return transactionRepository.findByWalletOrderByTransactionDateDesc(wallet);
    }

    @Transactional
    public void addFunds(Passenger passenger, Double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        PassengerWallet wallet = getWallet(passenger);
        wallet.setBalance(wallet.getBalance() + amount);
        walletRepository.save(wallet);

        WalletTransaction txn = new WalletTransaction(wallet, amount, "CREDIT", "Added funds to wallet");
        transactionRepository.save(txn);
    }

    @Transactional
    public void deductFunds(Passenger passenger, Double amount, String description) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        PassengerWallet wallet = getWallet(passenger);
        if (wallet.getBalance() < amount) {
            throw new RuntimeException("Insufficient wallet balance");
        }
        wallet.setBalance(wallet.getBalance() - amount);
        walletRepository.save(wallet);

        WalletTransaction txn = new WalletTransaction(wallet, amount, "DEBIT", description);
        transactionRepository.save(txn);
    }
    
    @Transactional
    public void refundFunds(Passenger passenger, Double amount, String description) {
        if (amount <= 0) {
            return;
        }
        PassengerWallet wallet = getWallet(passenger);
        wallet.setBalance(wallet.getBalance() + amount);
        walletRepository.save(wallet);

        WalletTransaction txn = new WalletTransaction(wallet, amount, "CREDIT", description);
        transactionRepository.save(txn);
    }
}
