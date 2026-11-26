// src/main/java/com/irctc/pattern/observer/SMSNotifier.java
package com.irctc.pattern.observer;

import com.irctc.model.Ticket;
import com.irctc.model.enums.TicketStatus;
import org.springframework.stereotype.Component;

@Component
public class SMSNotifier implements TicketObserver {
    
    @Override
    public void update(Ticket ticket, TicketStatus oldStatus, TicketStatus newStatus) {
        String phoneNumber = ticket.getPassenger().getPhone();
        System.out.println("📱 SMS sent to " + phoneNumber);
        System.out.println("   Your ticket " + ticket.getPnr() + 
                          " status: " + newStatus.getDisplayName());
    }
}