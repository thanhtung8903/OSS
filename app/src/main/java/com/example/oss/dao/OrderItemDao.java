package com.example.oss.dao;

import androidx.room.*;
import androidx.lifecycle.LiveData;
import com.example.oss.entity.OrderItem;
import com.example.oss.entity.Product;
import java.util.List;
import java.math.BigDecimal;

@Dao
public interface OrderItemDao {
    @Query("SELECT * FROM order_items WHERE order_id = :orderId")
    LiveData<List<OrderItem>> getOrderItems(int orderId);

    @Query("SELECT * FROM order_items WHERE order_id = :orderId")
    List<OrderItem> getOrderItemsSync(int orderId);

    @Query("SELECT oi.*, p.name as product_name, p.image_url as product_image " +
            "FROM order_items oi " +
            "INNER JOIN products p ON oi.product_id = p.id " +
            "WHERE oi.order_id = :orderId")
    LiveData<List<OrderItemWithProduct>> getOrderItemsWithProduct(int orderId);

    @Query("SELECT * FROM order_items WHERE id = :id")
    LiveData<OrderItem> getOrderItemById(int id);

    @Query("SELECT COUNT(*) FROM order_items WHERE order_id = :orderId")
    LiveData<Integer> getOrderItemCount(int orderId);

    @Query("SELECT SUM(quantity * price_at_purchase) FROM order_items WHERE order_id = :orderId")
    LiveData<BigDecimal> getOrderTotal(int orderId);

    @Query("SELECT SUM(quantity * price_at_purchase) FROM order_items WHERE order_id = :orderId")
    BigDecimal getOrderTotalSync(int orderId);

    @Query("SELECT SUM(quantity) FROM order_items WHERE order_id = :orderId")
    LiveData<Integer> getTotalQuantityForOrder(int orderId);

    @Query("SELECT * FROM order_items WHERE product_id = :productId ORDER BY id DESC")
    LiveData<List<OrderItem>> getOrderItemsByProduct(int productId);

    @Insert
    long insertOrderItem(OrderItem orderItem);

    @Insert
    void insertOrderItems(List<OrderItem> orderItems);

    @Update
    void updateOrderItem(OrderItem orderItem);

    @Delete
    void deleteOrderItem(OrderItem orderItem);

    @Query("DELETE FROM order_items WHERE id = :orderItemId")
    void deleteOrderItemById(int orderItemId);

    @Query("DELETE FROM order_items WHERE order_id = :orderId")
    void deleteOrderItemsByOrderId(int orderId);

    // Method to check if user has purchased a specific product
    @Query("SELECT COUNT(*) FROM order_items oi " +
            "INNER JOIN orders o ON oi.order_id = o.id " +
            "WHERE o.user_id = :userId " +
            "AND oi.product_id = :productId " +
            "AND (o.status = 'delivered' OR o.status = 'shipped' OR o.status = 'confirmed')")
    int hasUserPurchasedProduct(int userId, int productId);

    // Inner class cho join query
    public static class OrderItemWithProduct {
        @Embedded
        public OrderItem orderItem;

        @ColumnInfo(name = "product_name")
        public String productName;

        @ColumnInfo(name = "product_image")
        public String productImage;
    }
}