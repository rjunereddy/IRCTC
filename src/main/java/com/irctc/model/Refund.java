package com.irctc.model;

import com.irctc.model.enums.RefundStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "refunds")
public class Refund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "ticket_pnr", nullable = false)
    private Ticket ticket;

    @Column(name = "refund_amount")
    private Double refundAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private RefundStatus status = RefundStatus.NONE;

    @Column(name = "requested_at")
    private LocalDateTime requestedAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "remarks")
    private String remarks;

    public Refund() {}

    public Refund(Ticket ticket, Double refundAmount) {
        this.ticket = ticket;
        this.refundAmount = refundAmount;
        this.status = RefundStatus.REQUESTED;
        this.requestedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Ticket getTicket() { return ticket; }
    public void setTicket(Ticket ticket) { this.ticket = ticket; }

    public Double getRefundAmount() { return refundAmount; }
    public void setRefundAmount(Double refundAmount) { this.refundAmount = refundAmount; }

    public RefundStatus getStatus() { return status; }
    public void setStatus(RefundStatus status) { this.status = status; }

    public LocalDateTime getRequestedAt() { return requestedAt; }
    public void setRequestedAt(LocalDateTime requestedAt) { this.requestedAt = requestedAt; }

    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
