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
import java.math.BigDecimal;
import java.util.Date;

@Entity(tableName = "orders", foreignKeys = {
        @ForeignKey(entity = User.class, parentColumns = "id", childColumns = "user_id", onDelete = ForeignKey.CASCADE),
        @ForeignKey(entity = Address.class, parentColumns = "id", childColumns = "shipping_address_id", onDelete = ForeignKey.SET_NULL)
}, indices = { @Index("user_id"), @Index("shipping_address_id") })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "user_id")
    private int userId;

    @ColumnInfo(name = "shipping_address_id")
    private Integer shippingAddressId;

    @ColumnInfo(name = "total_amount")
    private BigDecimal totalAmount;

    @ColumnInfo(name = "status")
    @Builder.Default
    private String status = "pending"; // pending, confirmed, shipped, delivered, cancelled

    @ColumnInfo(name = "order_date")
    @Builder.Default
    private Date orderDate = new Date();

    @ColumnInfo(name = "payment_method")
    private String paymentMethod; // cash, card, transfer
}