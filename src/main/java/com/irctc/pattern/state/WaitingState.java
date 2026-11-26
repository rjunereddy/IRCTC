package com.irctc.pattern.state;

import com.irctc.model.Ticket;
import com.irctc.model.enums.TicketStatus;
import java.time.LocalDate;

public class WaitingState implements TicketState {

    @Override
    public TicketStatus getStatus() {
        return TicketStatus.WAITING;
    }

    @Override
    public boolean canCancel(Ticket ticket) {
        if (ticket.getJourneyDate() == null || ticket.getJourneyDate().isBefore(LocalDate.now())) return false;
        return true;
    }

    @Override
    public boolean canPromote(Ticket ticket) {
        return true;
    }

    @Override
    public boolean canDownloadPdf(Ticket ticket) {
        return true;
    }

    @Override
    public double calculateRefund(Ticket ticket) {
        if (ticket.getTotalFare() == null || ticket.getTotalFare() <= 0) return 0.0;
        int paxCount = (ticket.getAllPassengers() != null && !ticket.getAllPassengers().isEmpty())
                ? ticket.getAllPassengers().size() : 1;
        double clerkage = paxCount * 60.0; // ₹60 per passenger clerkage fee
        return Math.max(0.0, Math.round((ticket.getTotalFare() - clerkage) * 100.0) / 100.0);
    }

    @Override
    public String getRefundExplanation(Ticket ticket) {
        int paxCount = (ticket.getAllPassengers() != null && !ticket.getAllPassengers().isEmpty())
                ? ticket.getAllPassengers().size() : 1;
        double clerkage = paxCount * 60.0;
        return "Full refund credited minus standard clerkage fee of ₹" + clerkage + " (₹60/passenger for Waitlist ticket).";
    }
}
