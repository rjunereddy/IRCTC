package com.irctc.pattern.state;

import com.irctc.model.Ticket;
import com.irctc.model.enums.TicketStatus;

public class RefundedState implements TicketState {

    @Override
    public TicketStatus getStatus() {
        return TicketStatus.REFUNDED;
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
        return "Ticket refund has already been processed and credited.";
    }
}
