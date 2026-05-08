package com.irctc.model;

import com.irctc.model.enums.PaymentStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class Payment {
    
    @Id
    @Column(length = 20)
    private String paymentId;
    
    @OneToOne
    @JoinColumn(name = "ticket_pnr")
    private Ticket ticket;
    
    private Double amount;
    
    @Column(name = "payment_date")
    private LocalDateTime paymentDate;
    
    @Column(name = "payment_method")
    private String paymentMethod;
    
    @Column(name = "transaction_id")
    private String transactionId;
    
    @Column(name = "bank_reference")
    private String bankReference;
    
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;
    
    @Column(name = "gateway_response")
    private String gatewayResponse;
    
    @Column(name = "retry_count")
    private Integer retryCount = 0;
    
    @Column(name = "failure_reason")
    private String failureReason;
    
    public Payment() {}
    
    public Payment(String paymentId, Ticket ticket, Double amount, String paymentMethod) {
        this.paymentId = paymentId;
        this.ticket = ticket;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.paymentDate = LocalDateTime.now();
        this.status = PaymentStatus.PENDING;
    }
    
    // Getters and Setters
    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }
    
    public Ticket getTicket() { return ticket; }
    public void setTicket(Ticket ticket) { this.ticket = ticket; }
    
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    
    public LocalDateTime getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDateTime paymentDate) { this.paymentDate = paymentDate; }
    
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    
    public String getBankReference() { return bankReference; }
    public void setBankReference(String bankReference) { this.bankReference = bankReference; }
    
    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }
    
    public String getGatewayResponse() { return gatewayResponse; }
    public void setGatewayResponse(String gatewayResponse) { this.gatewayResponse = gatewayResponse; }
    
    public Integer getRetryCount() { return retryCount; }
    public void setRetryCount(Integer retryCount) { this.retryCount = retryCount; }
    
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
    
    public boolean processPayment() {
        boolean success = Math.random() > 0.1;
        if (success) {
            this.status = PaymentStatus.SUCCESS;
            this.transactionId = "TXN" + System.currentTimeMillis();
            return true;
        } else {
            this.status = PaymentStatus.FAILED;
            this.failureReason = "Payment gateway error";
            return false;
        }
    }
}
