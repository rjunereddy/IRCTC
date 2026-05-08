package com.irctc.pattern.factory;

import com.irctc.model.Ticket;
import org.springframework.stereotype.Component;

/**
 * Senior Citizen Fare = base fare per class × 0.50 (50% discount) × passenger count.
 * Base fare is taken from the train's fareStructure, not a hardcoded constant.
 * IRCTC gives 40% discount to male senior citizens (60+) and 50% to female (58+).
 * For simplicity in this demo, a flat 50% discount is applied to all senior citizen passengers.
 */
@Component
public class SeniorCitizenFareCalculator implements FareCalculator {

    @Override
    public double calculateFare(Ticket ticket) {
        int passengerCount = ticket.getAllPassengers() != null ? ticket.getAllPassengers().size() : 1;
        double baseFarePerPassenger = GeneralFareCalculator.getFarePerPassenger(ticket);
        double seniorBaseTotal = baseFarePerPassenger * 0.50 * passengerCount;
        
        // Save base fare to ticket
        ticket.setBaseFare(seniorBaseTotal);
        
        // Calculate GST (5% for AC classes)
        double gstAmount = 0.0;
        if (isAcClass(ticket.getClassType())) {
            gstAmount = seniorBaseTotal * 0.05;
        }
        ticket.setGstAmount(gstAmount);
        
        double total = seniorBaseTotal + gstAmount;
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