package com.example.oss.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.example.oss.database.AppDatabase;
import com.example.oss.dao.OrderItemDao;
import com.example.oss.entity.OrderItem;
import java.util.List;
import java.math.BigDecimal;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class OrderItemRepository {
    private OrderItemDao orderItemDao;
    private ExecutorService executor;

    public OrderItemRepository(Application application) {
        AppDatabase database = AppDatabase.getDatabase(application);
        orderItemDao = database.orderItemDao();
        executor = Executors.newFixedThreadPool(2);
    }

    // Read operations
    public LiveData<List<OrderItem>> getOrderItems(int orderId) {
        return orderItemDao.getOrderItems(orderId);
    }

    public Future<List<OrderItem>> getOrderItemsSync(int orderId) {
        return executor.submit(() -> orderItemDao.getOrderItemsSync(orderId));
    }

    public LiveData<List<OrderItemDao.OrderItemWithProduct>> getOrderItemsWithProduct(int orderId) {
        return orderItemDao.getOrderItemsWithProduct(orderId);
    }

    public LiveData<OrderItem> getOrderItemById(int id) {
        return orderItemDao.getOrderItemById(id);
    }

    public LiveData<Integer> getOrderItemCount(int orderId) {
        return orderItemDao.getOrderItemCount(orderId);
    }

    public LiveData<BigDecimal> getOrderTotal(int orderId) {
        return orderItemDao.getOrderTotal(orderId);
    }

    public Future<BigDecimal> getOrderTotalSync(int orderId) {
        return executor.submit(() -> orderItemDao.getOrderTotalSync(orderId));
    }

    public LiveData<Integer> getTotalQuantityForOrder(int orderId) {
        return orderItemDao.getTotalQuantityForOrder(orderId);
    }

    public LiveData<List<OrderItem>> getOrderItemsByProduct(int productId) {
        return orderItemDao.getOrderItemsByProduct(productId);
    }

    // Write operations
    public void insertOrderItem(OrderItem orderItem) {
        executor.execute(() -> orderItemDao.insertOrderItem(orderItem));
    }

    public void insertOrderItems(List<OrderItem> orderItems) {
        executor.execute(() -> orderItemDao.insertOrderItems(orderItems));
    }

    public void updateOrderItem(OrderItem orderItem) {
        executor.execute(() -> orderItemDao.updateOrderItem(orderItem));
    }

    public void deleteOrderItem(OrderItem orderItem) {
        executor.execute(() -> orderItemDao.deleteOrderItem(orderItem));
    }

    public void deleteOrderItemById(int orderItemId) {
        executor.execute(() -> orderItemDao.deleteOrderItemById(orderItemId));
    }

    public void deleteOrderItemsByOrderId(int orderId) {
        executor.execute(() -> orderItemDao.deleteOrderItemsByOrderId(orderId));
    }

    // Business logic methods
    public void addOrderItem(int orderId, int productId, int quantity, BigDecimal priceAtPurchase) {
        OrderItem orderItem = OrderItem.builder()
                .orderId(orderId)
                .productId(productId)
                .quantity(quantity)
                .priceAtPurchase(priceAtPurchase)
                .build();
        insertOrderItem(orderItem);
    }

    public void updateOrderItemQuantity(int orderItemId, int newQuantity) {
        executor.execute(() -> {
            OrderItem orderItem = orderItemDao.getOrderItemById(orderItemId).getValue();
            if (orderItem != null) {
                orderItem.setQuantity(newQuantity);
                orderItemDao.updateOrderItem(orderItem);
            }
        });
    }

    public Future<BigDecimal> calculateItemTotal(int orderItemId) {
        return executor.submit(() -> {
            OrderItem orderItem = orderItemDao.getOrderItemById(orderItemId).getValue();
            if (orderItem != null) {
                return orderItem.getPriceAtPurchase()
                        .multiply(BigDecimal.valueOf(orderItem.getQuantity()));
            }
            return BigDecimal.ZERO;
        });
    }

    public Future<Boolean> hasOrderItems(int orderId) {
        return executor.submit(() -> {
            Integer count = orderItemDao.getOrderItemCount(orderId).getValue();
            return count != null && count > 0;
        });
    }

    public Future<List<OrderItem>> createOrderItemsFromCart(int orderId, List<CartItem> cartItems) {
        return executor.submit(() -> {
            List<OrderItem> orderItems = new java.util.ArrayList<>();

            for (CartItem cartItem : cartItems) {
                OrderItem orderItem = OrderItem.builder()
                        .orderId(orderId)
                        .productId(cartItem.getProductId())
                        .quantity(cartItem.getQuantity())
                        .priceAtPurchase(cartItem.getCurrentPrice())
                        .build();

                orderItems.add(orderItem);
            }

            orderItemDao.insertOrderItems(orderItems);
            return orderItems;
        });
    }

    public Future<Integer> getTotalItemsInOrder(int orderId) {
        return executor.submit(() -> {
            List<OrderItem> items = orderItemDao.getOrderItemsSync(orderId);
            return items.stream().mapToInt(OrderItem::getQuantity).sum();
        });
    }

    public Future<Boolean> canModifyOrderItems(int orderId) {
        return executor.submit(() -> {
            // Logic to check if order items can be modified
            // For example, only pending orders can be modified
            // This would require accessing Order status
            return true; // Placeholder
        });
    }

    // Utility methods
    public Future<String> getOrderSummary(int orderId) {
        return executor.submit(() -> {
            List<OrderItem> items = orderItemDao.getOrderItemsSync(orderId);
            int totalItems = items.size();
            int totalQuantity = items.stream().mapToInt(OrderItem::getQuantity).sum();
            BigDecimal totalAmount = orderItemDao.getOrderTotalSync(orderId);

            return String.format("Đơn hàng có %d sản phẩm, tổng số lượng: %d, tổng tiền: %s",
                    totalItems, totalQuantity, totalAmount.toString());
        });
    }

    // Inner class cho cart items (để convert sang order items)
    public static class CartItem {
        private int productId;
        private int quantity;
        private BigDecimal currentPrice;

        public CartItem(int productId, int quantity, BigDecimal currentPrice) {
            this.productId = productId;
            this.quantity = quantity;
            this.currentPrice = currentPrice;
        }

        // Getters
        public int getProductId() {
            return productId;
        }

        public int getQuantity() {
            return quantity;
        }

        public BigDecimal getCurrentPrice() {
            return currentPrice;
        }
    }
}