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
    }    public static UserRole fromString(String role) {
        android.util.Log.d("UserRole", "fromString called with: '" + role + "'");
        for (UserRole userRole : UserRole.values()) {
            android.util.Log.d("UserRole", "Comparing with value: '" + userRole.value + "' and name: '" + userRole.name() + "'");
            // Support both value ("Admin", "Customer") and enum name ("ADMIN", "CUSTOMER")
            if (userRole.value.equals(role) || userRole.name().equals(role)) {
                android.util.Log.d("UserRole", "Match found: " + userRole);
                return userRole;
            }
        }
        android.util.Log.d("UserRole", "No match found, returning CUSTOMER");
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