// src/main/java/com/irctc/pattern/observer/EmailNotifier.java
package com.irctc.pattern.observer;

import com.irctc.model.Ticket;
import com.irctc.model.enums.TicketStatus;
import org.springframework.stereotype.Component;

@Component
public class EmailNotifier implements TicketObserver {
    
    @Override
    public void update(Ticket ticket, TicketStatus oldStatus, TicketStatus newStatus) {
        String passengerEmail = ticket.getPassenger().getEmail();
        System.out.println("📧 Email sent to " + passengerEmail);
        System.out.println("   Subject: Ticket Status Update - " + ticket.getPnr());
        System.out.println("   Message: Your ticket status changed from " + 
                          oldStatus + " to " + newStatus);
    }
}