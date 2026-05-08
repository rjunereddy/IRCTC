package com.irctc.model.enums;

public enum QuotaType {
    GENERAL("General"),
    TATKAL("Tatkal"),
    LADIES("Ladies"),
    SENIOR_CITIZEN("Senior Citizen"),
    PREMIUM_TATKAL("Premium Tatkal"),
    FOREIGN_TOURIST("Foreign Tourist");

    private final String displayName;

    QuotaType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
