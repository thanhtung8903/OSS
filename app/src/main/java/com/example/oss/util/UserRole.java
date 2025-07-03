package com.example.oss.util;

public enum UserRole {
    ADMIN("Admin"),
    CUSTOMER("Customer");

    private final String value;

    UserRole(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static UserRole fromString(String role) {
        for (UserRole userRole : UserRole.values()) {
            if (userRole.value.equals(role)) {
                return userRole;
            }
        }
        return CUSTOMER; // Default
    }

    // Permission checks
    public boolean canAccessAdminPanel() {
        return this == ADMIN;
    }

    public boolean canManageProducts() {
        return this == ADMIN;
    }

    public boolean canManageUsers() {
        return this == ADMIN;
    }

    public boolean canViewOrders() {
        return true; // Both admin and customer can view orders
    }

    public boolean canManageAllOrders() {
        return this == ADMIN;
    }
}