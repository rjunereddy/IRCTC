package com.irctc.pattern.chain;

public class PaymentDetailsValidator extends BookingValidationHandler {

    @Override
    public void validate(BookingContext context) {
        String method = context.getRequest().getPaymentMethod();
        if (method == null || method.trim().isEmpty()) {
            throw new IllegalArgumentException("Payment method must be selected.");
        }

        // If paying via wallet, verify sufficient initial balance
        if ("wallet".equalsIgnoreCase(method)) {
            if (context.getWalletBalance() <= 0) {
                throw new IllegalStateException("Insufficient IRCTC wallet balance. Please add funds or choose another payment method.");
            }
        }

        passToNext(context);
    }
}
