package com.example.oss.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.Index;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.util.Date;

@Entity(tableName = "users", indices = {
        @Index(value = "email", unique = true),
        @Index(value = "phone_number", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "full_name")
    private String fullName;

    @ColumnInfo(name = "email")
    private String email;

    @ColumnInfo(name = "password")
    private String password;

    @ColumnInfo(name = "phone_number")
    private String phoneNumber;

    @ColumnInfo(name = "role")
    private String role; // ENUM: "admin", "customer"

    @ColumnInfo(name = "status")
    private String status; // ENUM: "active", "inactive", "banned"

    @ColumnInfo(name = "created_at")
    private Date createdAt;

    // Constructor without id and createdAt (for creating new users)
    public User(String fullName, String email, String password,
            String phoneNumber, String role, String status) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.status = status;
        this.createdAt = new Date();
    }
}