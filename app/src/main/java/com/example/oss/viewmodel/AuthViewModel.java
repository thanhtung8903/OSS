package com.example.oss.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.LiveData;
import com.example.oss.repository.UserRepository;
import com.example.oss.entity.User;
import com.example.oss.util.SecurityUtils;
import com.example.oss.util.SessionManager;
import com.example.oss.util.UserRole;
import com.example.oss.util.UserStatus;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import androidx.annotation.NonNull;

public class AuthViewModel extends AndroidViewModel {
    private UserRepository userRepository;
    private SessionManager sessionManager;
    private ExecutorService executor;

    private MutableLiveData<Boolean> isLoading;
    private MutableLiveData<String> errorMessage;
    private MutableLiveData<Boolean> isLoggedIn;
    private MutableLiveData<Boolean> registerSuccess;
    private MutableLiveData<SessionManager.SessionUser> currentUser;
    private MutableLiveData<LoginResult> loginResult;
    private MutableLiveData<Boolean> updateSuccess;

    // LoginResult class to encapsulate login response
    public static class LoginResult {
        private boolean success;
        private String errorMessage;

        public LoginResult(boolean success, String errorMessage) {
            this.success = success;
            this.errorMessage = errorMessage;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

    public AuthViewModel(@NonNull Application application) {
        super(application);
        userRepository = new UserRepository(application);
        sessionManager = SessionManager.getInstance(application);
        executor = Executors.newSingleThreadExecutor();

        isLoading = new MutableLiveData<>(false);
        errorMessage = new MutableLiveData<>();
        isLoggedIn = new MutableLiveData<>(sessionManager.isLoggedIn());
        registerSuccess = new MutableLiveData<>(false);
        currentUser = new MutableLiveData<>(sessionManager.getLoggedInUser());
        loginResult = new MutableLiveData<>();
        updateSuccess = new MutableLiveData<>();
    }

    // Getters for LiveData
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getIsLoggedIn() {
        return isLoggedIn;
    }

    public LiveData<Boolean> getRegisterSuccess() {
        return registerSuccess;
    }

    public LiveData<SessionManager.SessionUser> getCurrentUser() {
        return currentUser;
    }

    public LiveData<LoginResult> getLoginResult() {
        return loginResult;
    }

    public LiveData<Boolean> getUpdateSuccess() {
        return updateSuccess;
    }

    // Login method
    public void login(String email, String password, boolean rememberMe) {
        if (!validateLoginInput(email, password)) {
            return;
        }

        isLoading.postValue(true);
        errorMessage.postValue(null);

        Future<User> loginFuture = userRepository.login(email, password);

        new Thread(() -> {
            try {
                User user = loginFuture.get();
                isLoading.postValue(false);

                if (user != null) {
                    // Check user status
                    UserStatus status = UserStatus.fromString(user.getStatus());
                    if (!status.canLogin()) {
                        String message = status == UserStatus.BANNED ? "Tài khoản đã bị khóa"
                                : "Tài khoản không hoạt động";
                        errorMessage.postValue(message);
                        loginResult.postValue(new LoginResult(false, message));
                        return;
                    }

                    // Create session
                    sessionManager.createLoginSession(user, rememberMe);

                    // Update LiveData
                    isLoggedIn.postValue(true);
                    currentUser.postValue(sessionManager.getLoggedInUser());
                    loginResult.postValue(new LoginResult(true, null));

                } else {
                    String message = "Email hoặc mật khẩu không chính xác";
                    errorMessage.postValue(message);
                    loginResult.postValue(new LoginResult(false, message));
                }
            } catch (Exception e) {
                isLoading.postValue(false);
                String message = "Lỗi đăng nhập: " + e.getMessage();
                errorMessage.postValue(message);
                loginResult.postValue(new LoginResult(false, message));
            }
        }).start();
    }

    // Register method
    public void register(String fullName, String email, String password, String phoneNumber) {
        if (!validateRegisterInput(fullName, email, password, phoneNumber)) {
            return;
        }

        isLoading.postValue(true);
        errorMessage.postValue(null);
        registerSuccess.postValue(false);

        Future<Boolean> registerFuture = userRepository.register(fullName, email, password, phoneNumber);

        new Thread(() -> {
            try {
                Boolean success = registerFuture.get();
                isLoading.postValue(false);

                if (success) {
                    registerSuccess.postValue(true);
                } else {
                    errorMessage.postValue("Email đã tồn tại");
                }
            } catch (Exception e) {
                isLoading.postValue(false);
                errorMessage.postValue("Lỗi đăng ký: " + e.getMessage());
            }
        }).start();
    }

    // Logout method
    public void logout() {
        sessionManager.logoutUser();
        isLoggedIn.postValue(false);
        currentUser.postValue(null);
    }

    // Check session on app start
    public void checkSession() {
        boolean loggedIn = sessionManager.isLoggedIn();
        isLoggedIn.postValue(loggedIn);
        if (loggedIn) {
            currentUser.postValue(sessionManager.getLoggedInUser());
        }
    }

    // Permission checks
    public boolean hasPermission(String permission) {
        SessionManager.SessionUser user = sessionManager.getLoggedInUser();
        if (user == null)
            return false;

        switch (permission) {
            case "admin_panel":
                return user.getRole().canAccessAdminPanel();
            case "manage_products":
                return user.getRole().canManageProducts();
            case "manage_users":
                return user.getRole().canManageUsers();
            case "view_orders":
                return user.getRole().canViewOrders();
            case "manage_all_orders":
                return user.getRole().canManageAllOrders();
            default:
                return false;
        }
    }

    // Role checks
    public boolean isAdmin() {
        return sessionManager.isAdmin();
    }

    public boolean isCustomer() {
        return sessionManager.isCustomer();
    }

    // Validation methods
    private boolean validateLoginInput(String email, String password) {
        if (email == null || email.trim().isEmpty()) {
            errorMessage.postValue("Email không được để trống");
            return false;
        }

        if (!SecurityUtils.isValidEmail(email)) {
            errorMessage.postValue("Email không hợp lệ");
            return false;
        }

        if (password == null || password.isEmpty()) {
            errorMessage.postValue("Mật khẩu không được để trống");
            return false;
        }

        return true;
    }

    private boolean validateRegisterInput(String fullName, String email, String password, String phoneNumber) {
        if (fullName == null || fullName.trim().isEmpty()) {
            errorMessage.postValue("Họ tên không được để trống");
            return false;
        }

        if (email == null || email.trim().isEmpty()) {
            errorMessage.postValue("Email không được để trống");
            return false;
        }

        if (!SecurityUtils.isValidEmail(email)) {
            errorMessage.postValue("Email không hợp lệ");
            return false;
        }

        if (password == null || password.isEmpty()) {
            errorMessage.postValue("Mật khẩu không được để trống");
            return false;
        }

        if (!SecurityUtils.isPasswordStrong(password)) {
            errorMessage
                    .postValue("Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường, số và ký tự đặc biệt");
            return false;
        }

        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            errorMessage.postValue("Số điện thoại không được để trống");
            return false;
        }

        if (!phoneNumber.matches("\\d{10,11}")) {
            errorMessage.postValue("Số điện thoại không hợp lệ");
            return false;
        }

        return true;
    }

    // Clear methods
    public void clearError() {
        errorMessage.postValue(null);
    }

    public void clearRegisterSuccess() {
        registerSuccess.postValue(false);
    }

    // Update profile without password change
    public void updateProfile(User updatedUser) {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        executor.execute(() -> {
            try {
                userRepository.updateUser(updatedUser);

                // Update session với new data
                SessionManager sessionManager = SessionManager.getInstance(getApplication());
                sessionManager.createLoginSession(updatedUser,
                        sessionManager.getLoggedInUser() != null); // preserve remember me setting

                updateSuccess.postValue(true);
            } catch (Exception e) {
                errorMessage.postValue("Lỗi khi cập nhật thông tin: " + e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        });
    }

    // Update profile with password change
    public void updateProfileWithPassword(User updatedUser, String currentPassword, String newPassword) {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        executor.execute(() -> {
            try {
                // Verify current password first
                User existingUser = userRepository.getUserByIdSync(updatedUser.getId());
                if (existingUser == null) {
                    errorMessage.postValue("Không tìm thấy thông tin người dùng");
                    return;
                }

                String hashedCurrentPassword = SecurityUtils.hashPassword(currentPassword);
                if (!hashedCurrentPassword.equals(existingUser.getPassword())) {
                    errorMessage.postValue("Mật khẩu hiện tại không đúng");
                    return;
                }

                // Hash new password và update
                String hashedNewPassword = SecurityUtils.hashPassword(newPassword);
                updatedUser.setPassword(hashedNewPassword);

                userRepository.updateUser(updatedUser);

                // Update session với new data
                SessionManager sessionManager = SessionManager.getInstance(getApplication());
                sessionManager.createLoginSession(updatedUser,
                        sessionManager.getLoggedInUser() != null); // preserve remember me setting

                updateSuccess.postValue(true);
            } catch (Exception e) {
                errorMessage.postValue("Lỗi khi cập nhật thông tin: " + e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        });
    }

    public void clearUpdateSuccess() {
        updateSuccess.setValue(null);
    }

    // Change password only (separated method)
    public void changePassword(int userId, String currentPassword, String newPassword) {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        executor.execute(() -> {
            try {
                // Verify current password first
                User existingUser = userRepository.getUserByIdSync(userId);
                if (existingUser == null) {
                    errorMessage.postValue("Không tìm thấy thông tin người dùng");
                    return;
                }

                String hashedCurrentPassword = SecurityUtils.hashPassword(currentPassword);
                if (!hashedCurrentPassword.equals(existingUser.getPassword())) {
                    errorMessage.postValue("Mật khẩu hiện tại không đúng");
                    return;
                }

                // Hash new password và update
                String hashedNewPassword = SecurityUtils.hashPassword(newPassword);
                existingUser.setPassword(hashedNewPassword);

                userRepository.updateUser(existingUser);

                updateSuccess.postValue(true);
            } catch (Exception e) {
                errorMessage.postValue("Lỗi khi đổi mật khẩu: " + e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        });
    }
}