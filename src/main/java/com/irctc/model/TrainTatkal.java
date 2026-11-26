package com.irctc.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "train_tatkal")
public class TrainTatkal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "train_no", nullable = false)
    private Train train;

    @Column(name = "journey_date", nullable = false)
    private LocalDate journeyDate;

    // Default constructor
    public TrainTatkal() {}

    public TrainTatkal(Train train, LocalDate journeyDate) {
        this.train = train;
        this.journeyDate = journeyDate;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Train getTrain() {
        return train;
    }

    public void setTrain(Train train) {
        this.train = train;
    }

    public LocalDate getJourneyDate() {
        return journeyDate;
    }

    public void setJourneyDate(LocalDate journeyDate) {
        this.journeyDate = journeyDate;
    }
}
