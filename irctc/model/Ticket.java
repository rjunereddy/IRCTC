package com.irctc.model;

import com.irctc.model.enums.ClassType;
import com.irctc.model.enums.QuotaType;
import com.irctc.model.enums.RefundStatus;
import com.irctc.model.enums.TicketStatus;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tickets")
public class Ticket {
    
    @Id
    @Column(length = 10)
    private String pnr;
    
    @Column(name = "booking_date")
    private LocalDateTime bookingDate;
    
    @Column(name = "journey_date")
    private LocalDate journeyDate;
    
    @ManyToOne
    @JoinColumn(name = "train_no")
    private Train train;
    
    @ManyToOne
    @JoinColumn(name = "passenger_id")
    private Passenger passenger;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "ticket")
    private List<PassengerDetail> allPassengers = new ArrayList<>();
    
    @Enumerated(EnumType.STRING)
    private ClassType classType;
    
    @Enumerated(EnumType.STRING)
    private QuotaType quotaType;
    
    private String coachNumber;
    
    @ElementCollection
    @CollectionTable(name = "ticket_seats")
    @Column(name = "seat_number")
    private List<String> seatNumbers = new ArrayList<>();
    
    @Column(name = "total_fare")
    private Double totalFare;

    @Column(name = "base_fare")
    private Double baseFare = 0.0;

    @Column(name = "gst_amount")
    private Double gstAmount = 0.0;

    @Column(name = "service_fee")
    private Double serviceFee = 15.0;

    @Column(name = "insurance_amount")
    private Double insuranceAmount = 0.0;

    @Column(name = "food_total")
    private Double foodTotal = 0.0;

    @Column(name = "food_items", length = 2000)
    private String foodItems;
    
    @Enumerated(EnumType.STRING)
    private TicketStatus status;
    
    @Column(name = "waitlist_number")
    private Integer waitlistNumber;
    
    @Column(name = "rac_number")
    private Integer racNumber;
    
    @Column(name = "chart_prepared")
    private boolean chartPrepared = false;
    
    @Column(name = "transaction_id")
    private String transactionId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "refund_status")
    private RefundStatus refundStatus = RefundStatus.NONE;
    
    @OneToOne(mappedBy = "ticket", cascade = CascadeType.ALL)
    private Payment payment;
    
    public Ticket() {}
    
    public Ticket(String pnr, Train train, Passenger passenger, LocalDate journeyDate,
                  ClassType classType, QuotaType quotaType, Double totalFare) {
        this.pnr = pnr;
        this.train = train;
        this.passenger = passenger;
        this.journeyDate = journeyDate;
        this.classType = classType;
        this.quotaType = quotaType;
        this.totalFare = totalFare;
        this.bookingDate = LocalDateTime.now();
        this.status = TicketStatus.CONFIRMED;
    }
    
    // Getters and Setters
    public String getPnr() { return pnr; }
    public void setPnr(String pnr) { this.pnr = pnr; }
    
    public LocalDateTime getBookingDate() { return bookingDate; }
    public void setBookingDate(LocalDateTime bookingDate) { this.bookingDate = bookingDate; }
    
    public LocalDate getJourneyDate() { return journeyDate; }
    public void setJourneyDate(LocalDate journeyDate) { this.journeyDate = journeyDate; }
    
    public Train getTrain() { return train; }
    public void setTrain(Train train) { this.train = train; }
    
    public Passenger getPassenger() { return passenger; }
    public void setPassenger(Passenger passenger) { this.passenger = passenger; }
    
    public List<PassengerDetail> getAllPassengers() { return allPassengers; }
    public void setAllPassengers(List<PassengerDetail> allPassengers) { this.allPassengers = allPassengers; }
    
    public ClassType getClassType() { return classType; }
    public void setClassType(ClassType classType) { this.classType = classType; }
    
    public QuotaType getQuotaType() { return quotaType; }
    public void setQuotaType(QuotaType quotaType) { this.quotaType = quotaType; }
    
    public String getCoachNumber() { return coachNumber; }
    public void setCoachNumber(String coachNumber) { this.coachNumber = coachNumber; }
    
    public List<String> getSeatNumbers() { return seatNumbers; }
    public void setSeatNumbers(List<String> seatNumbers) { this.seatNumbers = seatNumbers; }
    
    public Double getTotalFare() { return totalFare; }
    public void setTotalFare(Double totalFare) { this.totalFare = totalFare; }

    public Double getBaseFare() { return baseFare; }
    public void setBaseFare(Double baseFare) { this.baseFare = baseFare; }

    public Double getGstAmount() { return gstAmount; }
    public void setGstAmount(Double gstAmount) { this.gstAmount = gstAmount; }

    public Double getServiceFee() { return serviceFee; }
    public void setServiceFee(Double serviceFee) { this.serviceFee = serviceFee; }

    public Double getInsuranceAmount() { return insuranceAmount; }
    public void setInsuranceAmount(Double insuranceAmount) { this.insuranceAmount = insuranceAmount; }

    public Double getFoodTotal() { return foodTotal; }
    public void setFoodTotal(Double foodTotal) { this.foodTotal = foodTotal; }

    public String getFoodItems() { return foodItems; }
    public void setFoodItems(String foodItems) { this.foodItems = foodItems; }
    
    public TicketStatus getStatus() { return status; }
    public void setStatus(TicketStatus status) { this.status = status; }
    
    public Integer getWaitlistNumber() { return waitlistNumber; }
    public void setWaitlistNumber(Integer waitlistNumber) { this.waitlistNumber = waitlistNumber; }
    
    public Integer getRacNumber() { return racNumber; }
    public void setRacNumber(Integer racNumber) { this.racNumber = racNumber; }
    
    public boolean isChartPrepared() { return chartPrepared; }
    public void setChartPrepared(boolean chartPrepared) { this.chartPrepared = chartPrepared; }
    
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    
    public RefundStatus getRefundStatus() { return refundStatus; }
    public void setRefundStatus(RefundStatus refundStatus) { this.refundStatus = refundStatus; }
    
    public Payment getPayment() { return payment; }
    public void setPayment(Payment payment) { this.payment = payment; }
    
    public boolean canBeCancelled() {
        if (status == TicketStatus.CANCELLED) return false;
        if (status == TicketStatus.REFUNDED) return false;
        if (chartPrepared) return false;
        if (journeyDate.isBefore(LocalDate.now())) return false;
        if (train == null || train.getDepartureTime() == null) return false;
        return true;
    }
    
    public double getRefundAmount() {
        if (status == TicketStatus.WAITING || status == TicketStatus.RAC) {
            return totalFare != null ? totalFare : 0;
        }
        
        if (train == null || train.getDepartureTime() == null) {
            return 0;
        }
        
        long hoursBeforeDeparture = Duration.between(
            LocalDateTime.now(),
            journeyDate.atTime(train.getDepartureTime())
        ).toHours();
        
        if (hoursBeforeDeparture > 48) {
            return totalFare != null ? totalFare * 0.5 : 0;
        } else if (hoursBeforeDeparture > 12) {
            return totalFare != null ? totalFare * 0.25 : 0;
        } else if (hoursBeforeDeparture > 4) {
            return totalFare != null ? totalFare * 0.1 : 0;
        }
        return 0.0;
    }
}