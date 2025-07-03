package com.example.oss.dao;

import androidx.room.*;
import androidx.lifecycle.LiveData;
import com.example.oss.entity.User;
import java.util.List;

@Dao
public interface UserDao {
    @Query("SELECT * FROM users WHERE LOWER(role) = 'customer' ORDER BY created_at DESC")
    LiveData<List<User>> getAllUsers();


    @Query("SELECT * FROM users WHERE id = :id")
    LiveData<User> getUserById(int id);

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    User getUserByEmail(String email);

    @Query("SELECT * FROM users WHERE email = :email AND password = :passwordHash LIMIT 1")
    User login(String email, String passwordHash);

    @Insert
    long insertUser(User user);

    @Update
    void updateUser(User user);

    @Delete
    void deleteUser(User user);

    @Query("SELECT * FROM users WHERE id = :userId")
    User getUserByIdSync(int userId);

    // Update status
    @Query("UPDATE users SET status = :status WHERE id = :userId")
    void updateUserStatus(int userId, String status);

    // Methods cho sample data
    @Query("SELECT COUNT(*) FROM users")
    int getUserCountSync();

    @Query("DELETE FROM users")
    void deleteAllUsers();
}