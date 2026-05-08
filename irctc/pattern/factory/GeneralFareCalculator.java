// src/main/java/com/irctc/pattern/factory/GeneralFareCalculator.java
package com.irctc.pattern.factory;

import com.irctc.model.Ticket;
import com.irctc.model.Train;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * General Fare Calculator (Factory Pattern).
 * Uses the train-specific fareStructure (populated per class in DataInitializer).
 * Falls back to ClassType.getBaseFare() if no structure defined.
 */
@Component
public class GeneralFareCalculator implements FareCalculator {

    @Override
    public double calculateFare(Ticket ticket) {
        int passengerCount = ticket.getAllPassengers() != null ? ticket.getAllPassengers().size() : 1;
        double baseFareTotal = getFarePerPassenger(ticket) * passengerCount;
        
        // Save base fare to ticket
        ticket.setBaseFare(baseFareTotal);
        
        // Calculate GST (5% for AC classes)
        double gstAmount = 0.0;
        if (isAcClass(ticket.getClassType())) {
            gstAmount = baseFareTotal * 0.05;
        }
        ticket.setGstAmount(gstAmount);
        
        double total = baseFareTotal + gstAmount;
        return Math.round(total * 100.0) / 100.0;
    }

    private boolean isAcClass(com.irctc.model.enums.ClassType classType) {
        if (classType == null) return false;
        return classType == com.irctc.model.enums.ClassType.FIRST_AC ||
               classType == com.irctc.model.enums.ClassType.SECOND_AC ||
               classType == com.irctc.model.enums.ClassType.THIRD_AC ||
               classType == com.irctc.model.enums.ClassType.AC_CHAIR_CAR;
    }

    /** Accessible by other calculators (Tatkal, Senior) for base fare lookup. */
    public static double getFarePerPassenger(Ticket ticket) {
        Train train = ticket.getTrain();
        if (train != null && ticket.getClassType() != null) {
            Map<String, Double> fareStructure = train.getFareStructure();
            if (fareStructure != null && fareStructure.containsKey(ticket.getClassType().name())) {
                return fareStructure.get(ticket.getClassType().name());
            }
        }
        // Fallback — ClassType hardcoded base fare
        return ticket.getClassType() != null ? (double) ticket.getClassType().getBaseFare() : 100.0;
    }
}