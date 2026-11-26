package com.irctc.pattern.state;

import com.irctc.model.Ticket;
import com.irctc.model.enums.TicketStatus;

public class CancelledState implements TicketState {

    @Override
    public TicketStatus getStatus() {
        return TicketStatus.CANCELLED;
    }

    @Override
    public boolean canCancel(Ticket ticket) {
        return false;
    }

    @Override
    public boolean canPromote(Ticket ticket) {
        return false;
    }

    @Override
    public boolean canDownloadPdf(Ticket ticket) {
        return false;
    }

    @Override
    public double calculateRefund(Ticket ticket) {
        return 0.0;
    }

    @Override
    public String getRefundExplanation(Ticket ticket) {
        return "Ticket is already cancelled.";
    }
}
