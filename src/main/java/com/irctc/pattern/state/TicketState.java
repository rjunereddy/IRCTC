package com.irctc.pattern.state;

import com.irctc.model.Ticket;
import com.irctc.model.enums.TicketStatus;

public interface TicketState {
    TicketStatus getStatus();
    boolean canCancel(Ticket ticket);
    boolean canPromote(Ticket ticket);
    boolean canDownloadPdf(Ticket ticket);
    double calculateRefund(Ticket ticket);
    String getRefundExplanation(Ticket ticket);
}
