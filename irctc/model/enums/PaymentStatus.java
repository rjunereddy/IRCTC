package com.irctc.model.enums;

public enum PaymentStatus {
    SUCCESS("Success"),
    FAILED("Failed"),
    PENDING("Pending"),
    REFUNDED("Refunded");

    private final String displayName;

    PaymentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
