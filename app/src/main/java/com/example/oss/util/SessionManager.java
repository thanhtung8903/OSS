package com.example.oss.util;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.oss.entity.User;
import java.util.Date;

public class SessionManager {
    private static final String PREF_NAME = "UserSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_USER_PHONE = "userPhone";
    private static final String KEY_USER_ROLE = "userRole";
    private static final String KEY_USER_STATUS = "userStatus";
    private static final String KEY_LOGIN_TIME = "loginTime";
    private static final String KEY_REMEMBER_ME = "rememberMe";

    // Session timeout (24 hours)
    private static final long SESSION_TIMEOUT = 24 * 60 * 60 * 1000; // 24 hours in milliseconds

    private static SessionManager instance;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private Context context;

    private SessionManager(Context context) {
        this.context = context;
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = preferences.edit();
    }

    // Singleton getInstance method
    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context.getApplicationContext());
        }
        return instance;
    }

    // Create login session
    public void createLoginSession(User user, boolean rememberMe) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putInt(KEY_USER_ID, user.getId());
        editor.putString(KEY_USER_EMAIL, user.getEmail());
        editor.putString(KEY_USER_NAME, user.getFullName());
        editor.putString(KEY_USER_PHONE, user.getPhoneNumber());
        editor.putString(KEY_USER_ROLE, user.getRole());
        editor.putString(KEY_USER_STATUS, user.getStatus());
        editor.putLong(KEY_LOGIN_TIME, System.currentTimeMillis());
        editor.putBoolean(KEY_REMEMBER_ME, rememberMe);
        editor.apply();
    }

    // Check if user is logged in
    public boolean isLoggedIn() {
        if (!preferences.getBoolean(KEY_IS_LOGGED_IN, false)) {
            return false;
        }

        // Check session timeout (if not remember me)
        if (!preferences.getBoolean(KEY_REMEMBER_ME, false)) {
            long loginTime = preferences.getLong(KEY_LOGIN_TIME, 0);
            long currentTime = System.currentTimeMillis();
            if (currentTime - loginTime > SESSION_TIMEOUT) {
                logoutUser();
                return false;
            }
        }

        return true;
    }

    // Get user details from session
    public SessionUser getLoggedInUser() {
        if (!isLoggedIn()) {
            return null;
        }

        return new SessionUser(
                preferences.getInt(KEY_USER_ID, -1),
                preferences.getString(KEY_USER_EMAIL, ""),
                preferences.getString(KEY_USER_NAME, ""),
                preferences.getString(KEY_USER_PHONE, ""),
                UserRole.fromString(preferences.getString(KEY_USER_ROLE, "customer")),
                UserStatus.fromString(preferences.getString(KEY_USER_STATUS, "active")));
    }

    // THÊM METHOD getCurrentUser() - alias for getLoggedInUser() để tương thích với
    // code khác
    public SessionUser getCurrentUser() {
        return getLoggedInUser();
    }

    // THÊM METHOD getCurrentUserId() để dễ dàng lấy user ID
    public int getCurrentUserId() {
        SessionUser user = getCurrentUser();
        return user != null ? user.getId() : -1;
    }

    // THÊM METHOD isCurrentUserLoggedIn() để check state
    public boolean isCurrentUserLoggedIn() {
        return getCurrentUser() != null;
    }

    // Check user role
    public boolean hasRole(UserRole role) {
        SessionUser user = getCurrentUser();
        return user != null && user.getRole() == role;
    }

    // Check if user is admin
    public boolean isAdmin() {
        return hasRole(UserRole.ADMIN);
    }

    // Check if user is customer
    public boolean isCustomer() {
        return hasRole(UserRole.CUSTOMER);
    }

    // Check user permissions
    public boolean canAccessAdminPanel() {
        SessionUser user = getCurrentUser();
        return user != null && user.getRole().canAccessAdminPanel();
    }

    public boolean canManageProducts() {
        SessionUser user = getCurrentUser();
        return user != null && user.getRole().canManageProducts();
    }

    public boolean canManageUsers() {
        SessionUser user = getCurrentUser();
        return user != null && user.getRole().canManageUsers();
    }

    // Update session (extend timeout)
    public void updateSession() {
        if (isLoggedIn()) {
            editor.putLong(KEY_LOGIN_TIME, System.currentTimeMillis());
            editor.apply();
        }
    }

    // Logout user
    public void logoutUser() {
        editor.clear();
        editor.apply();
    }

    // THÊM METHOD để get session info
    public String getSessionInfo() {
        SessionUser user = getCurrentUser();
        if (user == null) {
            return "Chưa đăng nhập";
        }

        long loginTime = preferences.getLong(KEY_LOGIN_TIME, 0);
        boolean rememberMe = preferences.getBoolean(KEY_REMEMBER_ME, false);

        return String.format("User: %s (%s) - Role: %s - Login: %s - Remember: %s",
                user.getFullName(),
                user.getEmail(),
                user.getRole().toString(),
                new Date(loginTime).toString(),
                rememberMe ? "Yes" : "No");
    }

    // Session User class
    public static class SessionUser {
        private int id;
        private String email;
        private String fullName;
        private String phoneNumber;
        private UserRole role;
        private UserStatus status;

        public SessionUser(int id, String email, String fullName, String phoneNumber, UserRole role,
                UserStatus status) {
            this.id = id;
            this.email = email;
            this.fullName = fullName;
            this.phoneNumber = phoneNumber;
            this.role = role;
            this.status = status;
        }

        // Getters
        public int getId() {
            return id;
        }

        public String getEmail() {
            return email;
        }

        public String getFullName() {
            return fullName;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public UserRole getRole() {
            return role;
        }

        public UserStatus getStatus() {
            return status;
        }

        public boolean canLogin() {
            return status.canLogin();
        }

        // THÊM METHOD toString() để debug dễ dàng
        @Override
        public String toString() {
            return String.format("SessionUser{id=%d, email='%s', fullName='%s', role=%s, status=%s}",
                    id, email, fullName, role, status);
        }

        // THÊM METHOD để check permissions nhanh
        public boolean isAdmin() {
            return role == UserRole.ADMIN;
        }

        public boolean isCustomer() {
            return role == UserRole.CUSTOMER;
        }

        public boolean isActive() {
            return status == UserStatus.ACTIVE;
        }
    }
}