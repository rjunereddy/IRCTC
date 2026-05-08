package com.irctc.model.enums;

public enum RefundStatus {
    NONE("No Refund Requested"),
    REQUESTED("Refund Requested"),
    APPROVED("Refund Approved"),
    REJECTED("Refund Rejected");

    private final String displayName;

    RefundStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
