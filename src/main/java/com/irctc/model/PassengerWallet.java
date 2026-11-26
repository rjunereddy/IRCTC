package com.irctc.model;

import jakarta.persistence.*;

@Entity
@Table(name = "passenger_wallets")
public class PassengerWallet {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "passenger_id", nullable = false)
    private Passenger passenger;

    @Column(nullable = false)
    private Double balance = 0.0;

    public PassengerWallet() {}

    public PassengerWallet(Passenger passenger) {
        this.passenger = passenger;
        this.balance = 0.0;
    }

    public Long getId() { return id; }
    public Passenger getPassenger() { return passenger; }
    public void setPassenger(Passenger passenger) { this.passenger = passenger; }
    public Double getBalance() { return balance; }
    public void setBalance(Double balance) { this.balance = balance; }
}
