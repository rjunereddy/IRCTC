// src/main/java/com/irctc/pattern/observer/TicketObserver.java
package com.irctc.pattern.observer;

import com.irctc.model.Ticket;
import com.irctc.model.enums.TicketStatus;

public interface TicketObserver {
    void update(Ticket ticket, TicketStatus oldStatus, TicketStatus newStatus);
}