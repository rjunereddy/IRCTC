package com.irctc.pattern.chain;

public class BookingValidationPipeline {

    private final BookingValidationHandler head;

    public BookingValidationPipeline() {
        // Assemble Chain of Responsibility
        BookingValidationHandler p1 = new PassengerDetailsValidator();
        BookingValidationHandler p2 = new QuotaRuleValidator();
        BookingValidationHandler p3 = new SeatAvailabilityValidator();
        BookingValidationHandler p4 = new PaymentDetailsValidator();

        p1.setNext(p2).setNext(p3).setNext(p4);
        this.head = p1;
    }

    public void execute(BookingContext context) {
        head.validate(context);
    }
}
