package com.example.oss.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.oss.entity.User;
import com.example.oss.repository.UserRepository;
import java.util.List;

public class UserManagementViewModel extends AndroidViewModel {
    private UserRepository repository;
    private LiveData<List<User>> allUsers;

    public UserManagementViewModel(@NonNull Application application) {
        super(application);
        repository = new UserRepository(application);
        allUsers = repository.getAllUsers();
    }

    public LiveData<List<User>> getAllUsers() {
        return allUsers;
    }

    public LiveData<User> getUserById(int userId) {
        return repository.getUserById(userId);
    }

    public void updateUserStatus(int userId, String newStatus) {
        repository.updateUserStatus(userId, newStatus);
    }

    public boolean isEmailExists(String email) {
        return repository.isEmailExists(email);
    }

    public void createUser(User user) {
        repository.insertUser(user);
    }
} 