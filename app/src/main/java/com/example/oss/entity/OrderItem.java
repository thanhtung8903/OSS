package com.example.oss.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.Ignore;
import java.math.BigDecimal;

@Entity(tableName = "order_items", foreignKeys = {
        @ForeignKey(entity = Order.class, parentColumns = "id", childColumns = "order_id", onDelete = ForeignKey.CASCADE),
        @ForeignKey(entity = Product.class, parentColumns = "id", childColumns = "product_id", onDelete = ForeignKey.CASCADE)
}, indices = { @Index("order_id"), @Index("product_id") })
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

    // Additional fields for convenience (not stored in DB)
    @Ignore
    private String productName;

    @Ignore
    private String productImage;

    // Constructors
    public OrderItem() {
    }

    public OrderItem(int orderId, int productId, int quantity, BigDecimal priceAtPurchase) {
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.priceAtPurchase = priceAtPurchase;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPriceAtPurchase() {
        return priceAtPurchase;
    }

    public void setPriceAtPurchase(BigDecimal priceAtPurchase) {
        this.priceAtPurchase = priceAtPurchase;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductImage() {
        return productImage;
    }

    public void setProductImage(String productImage) {
        this.productImage = productImage;
    }

    // Convenience method to get price as double
    public double getPrice() {
        return priceAtPurchase != null ? priceAtPurchase.doubleValue() : 0.0;
    }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private OrderItem orderItem = new OrderItem();

        public Builder orderId(int orderId) {
            orderItem.orderId = orderId;
            return this;
        }

        public Builder productId(int productId) {
            orderItem.productId = productId;
            return this;
        }

        public Builder quantity(int quantity) {
            orderItem.quantity = quantity;
            return this;
        }

        public Builder priceAtPurchase(BigDecimal priceAtPurchase) {
            orderItem.priceAtPurchase = priceAtPurchase;
            return this;
        }

        public OrderItem build() {
            return orderItem;
        }
    }
}