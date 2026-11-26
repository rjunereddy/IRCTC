package com.irctc.pattern.chain;

public abstract class BookingValidationHandler {
    protected BookingValidationHandler next;

    public BookingValidationHandler setNext(BookingValidationHandler next) {
        this.next = next;
        return next;
    }

    public abstract void validate(BookingContext context);

    protected void passToNext(BookingContext context) {
        if (next != null) {
            next.validate(context);
        }
    }
}
