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

@Entity(tableName = "order_items", foreignKeys = {
        @ForeignKey(entity = Order.class, parentColumns = "id", childColumns = "order_id", onDelete = ForeignKey.CASCADE),
        @ForeignKey(entity = Product.class, parentColumns = "id", childColumns = "product_id", onDelete = ForeignKey.CASCADE)
}, indices = { @Index("order_id"), @Index("product_id") })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "order_id")
    private int orderId;

    @ColumnInfo(name = "product_id")
    private int productId;

    @ColumnInfo(name = "quantity")
    private int quantity;

    @ColumnInfo(name = "price_at_purchase")
    private BigDecimal priceAtPurchase;
}