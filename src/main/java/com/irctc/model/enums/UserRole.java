package com.irctc.model.enums;

public enum UserRole {
    PASSENGER("Passenger"),
    ADMIN("Administrator"),
    TICKET_COLLECTOR("Ticket Collector (TTE)"),
    STATION_MASTER("Station Master");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
