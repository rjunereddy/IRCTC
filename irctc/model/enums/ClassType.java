package com.irctc.model.enums;

public enum ClassType {
    FIRST_AC("1st AC", 1000),
    SECOND_AC("2nd AC", 600),
    THIRD_AC("3rd AC", 400),
    SLEEPER("Sleeper", 200),
    AC_CHAIR_CAR("AC Chair Car", 300),
    SECOND_SITTING("2nd Sitting", 100);

    private final String displayName;
    private final int baseFare;

    ClassType(String displayName, int baseFare) {
        this.displayName = displayName;
        this.baseFare = baseFare;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getBaseFare() {
        return baseFare;
    }
}
