package com.irctc.pattern.chain;

import com.irctc.dto.BookingRequest;
import java.util.List;

public class PassengerDetailsValidator extends BookingValidationHandler {

    @Override
    public void validate(BookingContext context) {
        List<BookingRequest.PassengerInfo> passengers = context.getRequest().getPassengers();
        if (passengers == null || passengers.isEmpty()) {
            throw new IllegalArgumentException("Booking must have at least one passenger.");
        }
        if (passengers.size() > 6) {
            throw new IllegalArgumentException("Maximum 6 passengers allowed per booking request.");
        }

        for (int i = 0; i < passengers.size(); i++) {
            BookingRequest.PassengerInfo p = passengers.get(i);
            if (p.getName() == null || p.getName().trim().isEmpty()) {
                throw new IllegalArgumentException("Passenger #" + (i + 1) + " name cannot be empty.");
            }
            if (p.getAge() == null || p.getAge() <= 0 || p.getAge() > 125) {
                throw new IllegalArgumentException("Passenger #" + (i + 1) + " must have a valid age between 1 and 125.");
            }
            if (p.getGender() == null || p.getGender().trim().isEmpty()) {
                throw new IllegalArgumentException("Passenger #" + (i + 1) + " must specify gender.");
            }
        }

        passToNext(context);
    }
}
