package com.example.oss.util;

public enum UserStatus {
    ACTIVE("active"),
    INACTIVE("inactive"),
    BANNED("banned");

    private final String value;

    UserStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static UserStatus fromString(String status) {
        for (UserStatus userStatus : UserStatus.values()) {
            if (userStatus.value.equals(status)) {
                return userStatus;
            }
        }
        return ACTIVE; // Default
    }

    public boolean canLogin() {
        return this == ACTIVE;
    }

    public boolean isBlocked() {
        return this == BANNED || this == INACTIVE;
    }
}