package com.irctc.pattern.chain;

import com.irctc.dto.BookingRequest;
import com.irctc.model.enums.QuotaType;

public class QuotaRuleValidator extends BookingValidationHandler {

    @Override
    public void validate(BookingContext context) {
        QuotaType quota = context.getRequest().getQuotaType();
        if (quota == null) {
            context.getRequest().setQuotaType(QuotaType.GENERAL);
            quota = QuotaType.GENERAL;
        }

        if (quota == QuotaType.TATKAL) {
            if (!context.isTatkalEnabled()) {
                throw new IllegalStateException("Tatkal booking is not enabled for this train on the selected date.");
            }
        }

        if (quota == QuotaType.SENIOR_CITIZEN) {
            boolean hasSenior = context.getRequest().getPassengers().stream()
                .anyMatch(p -> p.getAge() != null && p.getAge() >= 60);
            if (!hasSenior) {
                throw new IllegalArgumentException("Senior Citizen quota requires at least one passenger aged 60 or above.");
            }
        }

        if (quota == QuotaType.LADIES) {
            boolean hasFemale = context.getRequest().getPassengers().stream()
                .anyMatch(p -> "FEMALE".equalsIgnoreCase(p.getGender()));
            if (!hasFemale) {
                throw new IllegalArgumentException("Ladies quota requires at least one female passenger.");
            }
        }

        passToNext(context);
    }
}
