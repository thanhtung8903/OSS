package com.example.oss.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.ForeignKey;
import androidx.room.Index;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Entity(tableName = "addresses",
        foreignKeys = @ForeignKey(entity = User.class, parentColumns = "id", childColumns = "user_id", onDelete = ForeignKey.CASCADE),
        indices = {
        @Index("user_id") })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "user_id")
    private int userId;

    @ColumnInfo(name = "receiver_name")
    private String receiverName;

    @ColumnInfo(name = "phone_number")
    private String phoneNumber;

    @ColumnInfo(name = "street_address")
    private String streetAddress;

    @ColumnInfo(name = "city")
    private String city;

    @ColumnInfo(name = "postal_code")
    private String postalCode;

    @ColumnInfo(name = "is_default")
    @Builder.Default
    private boolean isDefault = false;
}