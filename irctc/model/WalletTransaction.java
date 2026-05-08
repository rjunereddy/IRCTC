package com.irctc.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "wallet_transactions")
public class WalletTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "wallet_id", nullable = false)
    private PassengerWallet wallet;

    private Double amount;
    
    private String type; // "CREDIT" or "DEBIT"
    
    private String description;
    
    private LocalDateTime transactionDate;

    public WalletTransaction() {}

    public WalletTransaction(PassengerWallet wallet, Double amount, String type, String description) {
        this.wallet = wallet;
        this.amount = amount;
        this.type = type;
        this.description = description;
        this.transactionDate = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public PassengerWallet getWallet() { return wallet; }
    public void setWallet(PassengerWallet wallet) { this.wallet = wallet; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDateTime transactionDate) { this.transactionDate = transactionDate; }
}
