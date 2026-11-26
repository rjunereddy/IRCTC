package com.irctc.pattern.chain;

import com.irctc.dto.BookingRequest;
import com.irctc.model.Passenger;
import com.irctc.model.Train;
import com.irctc.model.enums.TicketStatus;
import java.time.LocalDate;

public class BookingContext {
    private final BookingRequest request;
    private final Passenger passenger;
    private final Train train;
    private final LocalDate journeyDate;
    private final boolean tatkalEnabled;
    private final double walletBalance;
    private final int bookedSeats;
    private final int racSeats;
    private final int waitlistSeats;

    // Derived during validation
    private TicketStatus assignedStatus = TicketStatus.CONFIRMED;
    private int waitlistNumber = 0;
    private int racNumber = 0;

    public BookingContext(BookingRequest request,
                          Passenger passenger,
                          Train train,
                          LocalDate journeyDate,
                          boolean tatkalEnabled,
                          double walletBalance,
                          int bookedSeats,
                          int racSeats,
                          int waitlistSeats) {
        this.request = request;
        this.passenger = passenger;
        this.train = train;
        this.journeyDate = journeyDate;
        this.tatkalEnabled = tatkalEnabled;
        this.walletBalance = walletBalance;
        this.bookedSeats = bookedSeats;
        this.racSeats = racSeats;
        this.waitlistSeats = waitlistSeats;
    }

    public BookingRequest getRequest() { return request; }
    public Passenger getPassenger() { return passenger; }
    public Train getTrain() { return train; }
    public LocalDate getJourneyDate() { return journeyDate; }
    public boolean isTatkalEnabled() { return tatkalEnabled; }
    public double getWalletBalance() { return walletBalance; }
    public int getBookedSeats() { return bookedSeats; }
    public int getRacSeats() { return racSeats; }
    public int getWaitlistSeats() { return waitlistSeats; }

    public TicketStatus getAssignedStatus() { return assignedStatus; }
    public void setAssignedStatus(TicketStatus assignedStatus) { this.assignedStatus = assignedStatus; }

    public int getWaitlistNumber() { return waitlistNumber; }
    public void setWaitlistNumber(int waitlistNumber) { this.waitlistNumber = waitlistNumber; }

    public int getRacNumber() { return racNumber; }
    public void setRacNumber(int racNumber) { this.racNumber = racNumber; }
}
