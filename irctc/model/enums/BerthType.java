package com.irctc.model.enums;

public enum BerthType {
    NONE("No Preference"),
    LOWER("Lower"),
    MIDDLE("Middle"),
    UPPER("Upper"),
    SIDE_LOWER("Side Lower"),
    SIDE_UPPER("Side Upper");

    private final String displayName;

    BerthType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
