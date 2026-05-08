package com.irctc.model.enums;

public enum TicketStatus {
    CONFIRMED("Confirmed"),
    WAITING("Waiting List"),
    RAC("Reservation Against Cancellation"),
    CANCELLED("Cancelled"),
    REFUNDED("Refunded"),
    EXPIRED("Expired");

    private final String displayName;

    TicketStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
