package com.irctc.pattern.factory;

import com.irctc.model.Ticket;
import org.springframework.stereotype.Component;

/**
 * Tatkal Fare = base fare per class × 1.30 (30% premium) × passenger count.
 * Base fare is taken from the train's fareStructure, not a hardcoded constant.
 */
@Component
public class TatkalFareCalculator implements FareCalculator {

    @Override
    public double calculateFare(Ticket ticket) {
        int passengerCount = ticket.getAllPassengers() != null ? ticket.getAllPassengers().size() : 1;
        double baseFarePerPassenger = GeneralFareCalculator.getFarePerPassenger(ticket);
        double tatkalBaseTotal = baseFarePerPassenger * 1.30 * passengerCount;
        
        // Save base fare to ticket
        ticket.setBaseFare(tatkalBaseTotal);
        
        // Calculate GST (5% for AC classes)
        double gstAmount = 0.0;
        if (isAcClass(ticket.getClassType())) {
            gstAmount = tatkalBaseTotal * 0.05;
        }
        ticket.setGstAmount(gstAmount);
        
        double total = tatkalBaseTotal + gstAmount;
        return Math.round(total * 100.0) / 100.0;
    }

    private boolean isAcClass(com.irctc.model.enums.ClassType classType) {
        if (classType == null) return false;
        return classType == com.irctc.model.enums.ClassType.FIRST_AC ||
               classType == com.irctc.model.enums.ClassType.SECOND_AC ||
               classType == com.irctc.model.enums.ClassType.THIRD_AC ||
               classType == com.irctc.model.enums.ClassType.AC_CHAIR_CAR;
    }
}