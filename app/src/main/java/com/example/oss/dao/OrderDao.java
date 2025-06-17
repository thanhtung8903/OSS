package com.example.oss.dao;

import androidx.room.*;
import androidx.lifecycle.LiveData;
import com.example.oss.entity.Order;
import java.util.List;
import java.util.Date;
import java.math.BigDecimal;

@Dao
public interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY order_date DESC")
    LiveData<List<Order>> getAllOrders();

    @Query("SELECT * FROM orders WHERE user_id = :userId ORDER BY order_date DESC")
    LiveData<List<Order>> getOrdersByUser(int userId);

    @Query("SELECT * FROM orders WHERE id = :id")
    LiveData<Order> getOrderById(int id);

    @Query("SELECT * FROM orders WHERE id = :id")
    Order getOrderByIdSync(int id);

    @Query("SELECT * FROM orders WHERE status = :status ORDER BY order_date DESC")
    LiveData<List<Order>> getOrdersByStatus(String status);

    @Query("SELECT * FROM orders WHERE user_id = :userId AND status = :status ORDER BY order_date DESC")
    LiveData<List<Order>> getUserOrdersByStatus(int userId, String status);

    @Query("SELECT COUNT(*) FROM orders WHERE user_id = :userId")
    LiveData<Integer> getUserOrderCount(int userId);

    @Query("SELECT COUNT(*) FROM orders WHERE status = :status")
    LiveData<Integer> getOrderCountByStatus(String status);

    @Query("SELECT SUM(total_amount) FROM orders WHERE user_id = :userId AND status IN ('confirmed', 'shipped', 'delivered')")
    LiveData<BigDecimal> getUserTotalSpent(int userId);

    @Query("SELECT * FROM orders WHERE order_date BETWEEN :startDate AND :endDate ORDER BY order_date DESC")
    LiveData<List<Order>> getOrdersByDateRange(Date startDate, Date endDate);

    @Insert
    long insertOrder(Order order);

    @Update
    void updateOrder(Order order);

    @Delete
    void deleteOrder(Order order);

    @Query("UPDATE orders SET status = :status WHERE id = :orderId")
    void updateOrderStatus(int orderId, String status);

    @Query("DELETE FROM orders WHERE id = :orderId")
    void deleteOrderById(int orderId);
}