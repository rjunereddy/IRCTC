package com.irctc.pattern.chain;

import com.irctc.model.Train;
import com.irctc.model.enums.TicketStatus;

public class SeatAvailabilityValidator extends BookingValidationHandler {

    @Override
    public void validate(BookingContext context) {
        Train train = context.getTrain();
        int totalSeats = train.getTotalSeats() != null ? train.getTotalSeats() : 0;
        int racCap = train.getRacCapacity() != null ? train.getRacCapacity() : 20;
        int wlCap = train.getWaitlistCapacity() != null ? train.getWaitlistCapacity() : 50;

        int reqSeats = context.getRequest().getPassengers().size();
        int availableConfirmed = Math.max(0, totalSeats - context.getBookedSeats());

        if (availableConfirmed >= reqSeats) {
            context.setAssignedStatus(TicketStatus.CONFIRMED);
        } else {
            int racCurrent = context.getRacSeats();
            if (racCurrent + reqSeats <= racCap) {
                context.setAssignedStatus(TicketStatus.RAC);
                context.setRacNumber(racCurrent + 1);
            } else {
                int wlCurrent = context.getWaitlistSeats();
                if (wlCurrent + reqSeats > wlCap) {
                    throw new IllegalStateException("Not enough seats available: Train, RAC and Waitlist capacity are completely full.");
                }
                context.setAssignedStatus(TicketStatus.WAITING);
                context.setWaitlistNumber(wlCurrent + 1);
            }
        }

        passToNext(context);
    }
}
