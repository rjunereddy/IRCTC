package com.irctc.model.enums;

public enum UserRole {
    PASSENGER("Passenger"),
    ADMIN("Administrator");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
