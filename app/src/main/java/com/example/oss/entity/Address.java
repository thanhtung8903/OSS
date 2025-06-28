package com.example.oss.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.Ignore;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Entity(tableName = "addresses", foreignKeys = @ForeignKey(entity = User.class, parentColumns = "id", childColumns = "user_id", onDelete = ForeignKey.CASCADE), indices = {
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

    @ColumnInfo(name = "district")
    private String district; // Quận/Huyện

    @ColumnInfo(name = "city")
    private String city; // Tỉnh/Thành phố

    @ColumnInfo(name = "postal_code")
    private String postalCode;

    @ColumnInfo(name = "is_default")
    @Builder.Default
    private boolean isDefault = false;

    @ColumnInfo(name = "address_type")
    @Builder.Default
    private String addressType = "HOME"; // HOME, OFFICE, OTHER

    @ColumnInfo(name = "notes")
    private String notes; // Ghi chú thêm cho địa chỉ

    // Constructor without id - for creating new addresses
    @Ignore
    public Address(int userId, String receiverName, String phoneNumber,
            String streetAddress, String district, String city,
            String postalCode, boolean isDefault, String addressType, String notes) {
        this.userId = userId;
        this.receiverName = receiverName;
        this.phoneNumber = phoneNumber;
        this.streetAddress = streetAddress;
        this.district = district;
        this.city = city;
        this.postalCode = postalCode;
        this.isDefault = isDefault;
        this.addressType = addressType;
        this.notes = notes;
    }

    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        if (streetAddress != null && !streetAddress.isEmpty()) {
            sb.append(streetAddress);
        }
        if (district != null && !district.isEmpty()) {
            if (sb.length() > 0)
                sb.append(", ");
            sb.append(district);
        }
        if (city != null && !city.isEmpty()) {
            if (sb.length() > 0)
                sb.append(", ");
            sb.append(city);
        }
        return sb.toString();
    }
}