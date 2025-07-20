package com.example.oss.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.oss.database.AppDatabase;
import com.example.oss.dao.UserDao;
import com.example.oss.entity.User;
import java.util.List;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import com.example.oss.util.SecurityUtils;
import com.example.oss.util.UserRole;
import com.example.oss.util.UserStatus;

public class UserRepository {
    private UserDao userDao;
    private LiveData<List<User>> allUsers;
    private ExecutorService executor;
    private MutableLiveData<User> currentUser;

    public UserRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        userDao = db.userDao();
        allUsers = userDao.getAllUsers();
        executor = Executors.newSingleThreadExecutor();
        currentUser = new MutableLiveData<>();
    }

    // Read operations
    public LiveData<List<User>> getAllUsers() {
        return allUsers;
    }

    public LiveData<User> getUserById(int userId) {
        return userDao.getUserById(userId);
    }

    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    // Authentication methods
    public Future<User> login(String email, String password) {
        return executor.submit(() -> {
            try {
                User user = userDao.getUserByEmail(email);
                if (user != null && SecurityUtils.verifyPassword(password, user.getPassword())) {
                    return user;
                }
                return null;
            } catch (Exception e) {
                return null;
            }
        });
    }

    public Future<Boolean> register(String fullName, String email, String password, String phoneNumber) {
        return executor.submit(() -> {
            try {
                // Hash password
                String hashedPassword = SecurityUtils.hashPassword(password);

                // Create new user with customer role
                User newUser = new User(
                        fullName,
                        email,
                        hashedPassword,
                        phoneNumber,
                        UserRole.CUSTOMER.getValue(), // Default role
                        UserStatus.ACTIVE.getValue() // Default status
                );

                long userId = userDao.insertUser(newUser);
                return userId > 0;
            } catch (Exception e) {
                return false;
            }
        });
    }

    public void logout() {
        currentUser.postValue(null);
    }

    public Future<Boolean> changePassword(int userId, String oldPassword, String newPassword) {
        return executor.submit(() -> {
            User user = userDao.getUserById(userId).getValue();
            if (user != null && user.getPassword().equals(SecurityUtils.hashPassword(oldPassword))) {
                user.setPassword(SecurityUtils.hashPassword(newPassword));
                userDao.updateUser(user);
                return true;
            }
            return false;
        });
    }

    // Write operations
    public void insertUser(User user) {
        executor.execute(() -> userDao.insertUser(user));
    }

    public void updateUser(User user) {
        executor.execute(() -> {
            userDao.updateUser(user);
            // Cập nhật current user nếu đang login
            if (currentUser.getValue() != null &&
                    currentUser.getValue().getId() == user.getId()) {
                currentUser.postValue(user);
            }
        });
    }

    public void deleteUser(User user) {
        executor.execute(() -> userDao.deleteUser(user));
    }

    // Business logic methods
    public void updateUserProfile(int userId, String fullName, String phoneNumber) {
        executor.execute(() -> {
            User user = userDao.getUserById(userId).getValue();
            if (user != null) {
                user.setFullName(fullName);
                user.setPhoneNumber(phoneNumber);
                updateUser(user);
            }
        });
    }

    public void changeUserStatus(int userId, String status) {
        executor.execute(() -> {
            User user = userDao.getUserById(userId).getValue();
            if (user != null) {
                user.setStatus(status);
                updateUser(user);
            }
        });
    }

    // Validation methods
    public boolean isValidPassword(String password) {
        return SecurityUtils.isPasswordStrong(password);
    }

    public boolean isValidEmail(String email) {
        return SecurityUtils.isValidEmail(email);
    }

    // Synchronous method để get user by ID (cho validation)
    public User getUserByIdSync(int userId) {
        try {
            return executor.submit(() -> userDao.getUserByIdSync(userId)).get();
        } catch (Exception e) {
            return null;
        }
    }

    public void updateUserStatus(int userId, String newStatus) {
        executor.execute(() -> userDao.updateUserStatus(userId, newStatus));
    }

    public boolean isEmailExists(String email) {
        try {
            User user = userDao.getUserByEmail(email);
            return user != null;
        } catch (Exception e) {
            return false;
        }
    }

    // Reset password methods
    public Future<Boolean> resetPassword(String email) {
        return executor.submit(() -> {
            try {
                // Kiểm tra email có tồn tại không
                User user = userDao.getUserByEmail(email);
                if (user == null) {
                    return false;
                }

                // Tạo mật khẩu mới
                String newPassword = SecurityUtils.generateRandomPassword();

                // Hash mật khẩu mới và cập nhật trong database
                String hashedPassword = SecurityUtils.hashPassword(newPassword);
                user.setPassword(hashedPassword);
                userDao.updateUser(user);

                // Gửi email với mật khẩu mới
                boolean emailSent = com.example.oss.util.MailGun.sendResetPasswordEmail(
                        user.getEmail(),
                        user.getFullName(),
                        newPassword);

                if (!emailSent) {
                    android.util.Log.e("UserRepository", "Failed to send reset email to: " + user.getEmail());
                    return false;
                }

                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        });
    }

    // Method để lấy user theo email cho forgot password
    public Future<User> getUserByEmail(String email) {
        return executor.submit(() -> {
            try {
                return userDao.getUserByEmail(email);
            } catch (Exception e) {
                return null;
            }
        });
    }
}