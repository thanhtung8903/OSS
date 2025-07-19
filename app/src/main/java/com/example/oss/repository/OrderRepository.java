package com.example.oss.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;

import com.example.oss.bean.OrderData;
import com.example.oss.bean.OrderStatistics;
import com.example.oss.database.AppDatabase;
import com.example.oss.dao.OrderDao;
import com.example.oss.dao.OrderItemDao;
import com.example.oss.dao.ProductDao;
import com.example.oss.entity.Order;
import com.example.oss.entity.OrderItem;
import com.example.oss.entity.Product;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Date;
import java.math.BigDecimal;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class OrderRepository {
    private OrderDao orderDao;
    private OrderItemDao orderItemDao;
    private ProductDao productDao;
    private ExecutorService executor;

    public OrderRepository(Application application) {
        AppDatabase database = AppDatabase.getDatabase(application);
        orderDao = database.orderDao();
        orderItemDao = database.orderItemDao();
        productDao = database.productDao();
        executor = Executors.newFixedThreadPool(2);
    }

    // Read operations
    public LiveData<List<Order>> getAllOrders() {
        return orderDao.getAllOrders();
    }

    public LiveData<List<Order>> getOrdersByUser(int userId) {
        return orderDao.getOrdersByUser(userId);
    }

    public LiveData<OrderData> getAllOrderManagementDisplays() {
        MediatorLiveData<OrderData> result = new MediatorLiveData<>();

        // Lấy danh sách Orders kèm thông tin khách hàng
        LiveData<List<OrderDao.OrdersWithCustomer>> ordersLiveData = orderDao.getOrdersWithCustomer();

        ordersLiveData.observeForever(new Observer<>() {
            @Override
            public void onChanged(List<OrderDao.OrdersWithCustomer> ordersWithCustomers) {
                // Remove để tránh gọi lại không cần thiết
                ordersLiveData.removeObserver(this);

                if (ordersWithCustomers == null || ordersWithCustomers.isEmpty()) {
                    result.setValue(new OrderData(new ArrayList<>(), new OrderStatistics(0, 0, 0)));
                    return;
                }

                List<com.example.oss.bean.OrderDisplay> tempList = Collections.synchronizedList(new ArrayList<>());
                AtomicInteger loadedCount = new AtomicInteger(0);
                int totalOrders = ordersWithCustomers.size();

                // Sử dụng AtomicInteger để lưu trữ thống kê
                AtomicInteger pendingOrders = new AtomicInteger(0);
                AtomicInteger completedOrders = new AtomicInteger(0);

                for (OrderDao.OrdersWithCustomer orderWithCustomer : ordersWithCustomers) {
                    int orderId = orderWithCustomer.order.getId();
                    LiveData<List<OrderItemDao.OrderItemWithProduct>> itemsLiveData = orderItemDao.getOrderItemsWithProduct(orderId);

                    itemsLiveData.observeForever(new Observer<>() {
                        @Override
                        public void onChanged(List<OrderItemDao.OrderItemWithProduct> orderItems) {
                            // Gỡ observer để tránh memory leak
                            itemsLiveData.removeObserver(this);

                            com.example.oss.bean.OrderDisplay display = new com.example.oss.bean.OrderDisplay();
                            display.orderId = orderWithCustomer.order.getId();
                            display.orderDate = orderWithCustomer.order.getOrderDate();
                            display.orderStatus = orderWithCustomer.order.getStatus();
                            display.customerName = orderWithCustomer.customerName;
                            display.totalAmount = orderWithCustomer.order.getTotalAmount();
                            display.paymentMethod = orderWithCustomer.order.getPaymentMethod();

                            display.itemCount = 0;
                            StringBuilder sb = new StringBuilder();
                            for (OrderItemDao.OrderItemWithProduct item : orderItems) {
                                sb.append(item.productName)
                                        .append(" (x")
                                        .append(item.orderItem.getQuantity())
                                        .append("), ");
                                display.itemCount += item.orderItem.getQuantity();
                            }

                            if (sb.length() > 0) {
                                sb.setLength(sb.length() - 2); // Xóa dấu ", " cuối cùng
                            }

                            display.productSummary = sb.toString();
                            tempList.add(display);

                            switch (orderWithCustomer.order.getStatus()){
                                case "Chờ xử lý":
                                    pendingOrders.incrementAndGet();
                                    break;
                                case "Đã hoàn thành":
                                    completedOrders.incrementAndGet();
                                    break;
                                    // thêm các trạng thái khác nếu cần
                            }

                            // Khi đã xử lý đủ tất cả đơn hàng
                            if (loadedCount.incrementAndGet() == totalOrders) {
                                OrderStatistics statistics = new OrderStatistics(totalOrders, pendingOrders.get(), completedOrders.get());

                                OrderData orderData = new OrderData(new ArrayList<>(tempList), statistics);
                                result.setValue(orderData);
                            }
                        }
                    });
                }
            }
        });

        return result;
    }

    public LiveData<Order> getOrderById(int id) {
        return orderDao.getOrderById(id);
    }

    public Future<Order> getOrderByIdSync(int id) {
        return executor.submit(() -> orderDao.getOrderByIdSync(id));
    }

    public LiveData<List<Order>> getOrdersByStatus(String status) {
        return orderDao.getOrdersByStatus(status);
    }

    public LiveData<List<Order>> getUserOrdersByStatus(int userId, String status) {
        return orderDao.getUserOrdersByStatus(userId, status);
    }

    public LiveData<Integer> getUserOrderCount(int userId) {
        return orderDao.getUserOrderCount(userId);
    }

    public LiveData<Integer> getOrderCountByStatus(String status) {
        return orderDao.getOrderCountByStatus(status);
    }

    public LiveData<BigDecimal> getUserTotalSpent(int userId) {
        return orderDao.getUserTotalSpent(userId);
    }

    public LiveData<List<Order>> getOrdersByDateRange(Date startDate, Date endDate) {
        return orderDao.getOrdersByDateRange(startDate, endDate);
    }

    // Write operations
    public void insertOrder(Order order) {
        executor.execute(() -> orderDao.insertOrder(order));
    }

    public void updateOrder(Order order) {
        executor.execute(() -> orderDao.updateOrder(order));
    }

    public void deleteOrder(Order order) {
        executor.execute(() -> orderDao.deleteOrder(order));
    }

    public void updateOrderStatus(int orderId, String status) {
        executor.execute(() -> orderDao.updateOrderStatus(orderId, status));
    }

    public void deleteOrderById(int orderId) {
        executor.execute(() -> orderDao.deleteOrderById(orderId));
    }

    // Business logic methods
    public Future<Long> createOrder(int userId, Integer shippingAddressId,
            List<OrderItem> orderItems, String paymentMethod) {
        return executor.submit(() -> {
            // Tính tổng tiền
            BigDecimal totalAmount = calculateTotalAmount(orderItems);

            // Tạo order
            Order order = Order.builder()
                    .userId(userId)
                    .shippingAddressId(shippingAddressId)
                    .totalAmount(totalAmount)
                    .status("pending")
                    .orderDate(new Date())
                    .paymentMethod(paymentMethod)
                    .build();

            long orderId = orderDao.insertOrder(order);

            if (orderId > 0) {
                // Thêm order items
                for (OrderItem item : orderItems) {
                    item.setOrderId((int) orderId);
                    orderItemDao.insertOrderItem(item);
                }

                // Cập nhật stock quantity
                updateProductStock(orderItems);
            }

            return orderId;
        });
    }

    public void confirmOrder(int orderId) {
        updateOrderStatus(orderId, "confirmed");
    }

    public void shipOrder(int orderId) {
        updateOrderStatus(orderId, "shipped");
    }

    public void deliverOrder(int orderId) {
        updateOrderStatus(orderId, "delivered");
    }

    public void cancelOrder(int orderId) {
        executor.execute(() -> {
            // Hoàn trả stock
            List<OrderItem> orderItems = orderItemDao.getOrderItemsSync(orderId);
            restoreProductStock(orderItems);

            // Cập nhật status
            orderDao.updateOrderStatus(orderId, "cancelled");
        });
    }

    public Future<Boolean> canCancelOrder(int orderId) {
        return executor.submit(() -> {
            Order order = orderDao.getOrderByIdSync(orderId);
            return order != null &&
                    ("pending".equals(order.getStatus()) || "confirmed".equals(order.getStatus()));
        });
    }

    public Future<List<Order>> getPendingOrders() {
        return executor.submit(() -> orderDao.getOrdersByStatus("pending").getValue());
    }

    public Future<List<Order>> getShippedOrders() {
        return executor.submit(() -> orderDao.getOrdersByStatus("shipped").getValue());
    }

    // Utility methods
    private BigDecimal calculateTotalAmount(List<OrderItem> orderItems) {
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItem item : orderItems) {
            BigDecimal itemTotal = item.getPriceAtPurchase()
                    .multiply(BigDecimal.valueOf(item.getQuantity()));
            total = total.add(itemTotal);
        }
        return total;
    }

    private void updateProductStock(List<OrderItem> orderItems) {
        for (OrderItem item : orderItems) {
            Product product = productDao.getProductById(item.getProductId()).getValue();
            if (product != null) {
                int newStock = product.getStockQuantity() - item.getQuantity();
                product.setStockQuantity(Math.max(0, newStock)); // Prevent negative stock
                productDao.updateProduct(product);
            }
        }
    }

    private void restoreProductStock(List<OrderItem> orderItems) {
        for (OrderItem item : orderItems) {
            Product product = productDao.getProductById(item.getProductId()).getValue();
            if (product != null) {
                int newStock = product.getStockQuantity() + item.getQuantity();
                product.setStockQuantity(newStock);
                productDao.updateProduct(product);
            }
        }
    }

    // Order status constants
    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_CONFIRMED = "confirmed";
    public static final String STATUS_SHIPPED = "shipped";
    public static final String STATUS_DELIVERED = "delivered";
    public static final String STATUS_CANCELLED = "cancelled";
}