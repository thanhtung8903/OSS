package com.example.oss.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.oss.entity.Order;
import com.example.oss.entity.OrderItem;
import com.example.oss.repository.OrderRepository;
import com.example.oss.repository.OrderItemRepository;
import com.example.oss.repository.CartRepository;
import com.example.oss.util.SessionManager;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OrderHistoryViewModel extends AndroidViewModel {

    private OrderRepository orderRepository;
    private OrderItemRepository orderItemRepository;
    private CartRepository cartRepository;
    private SessionManager sessionManager;
    private ExecutorService executor;

    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private MutableLiveData<String> successMessage = new MutableLiveData<>();

    public OrderHistoryViewModel(@NonNull Application application) {
        super(application);
        orderRepository = new OrderRepository(application);
        orderItemRepository = new OrderItemRepository(application);
        cartRepository = new CartRepository(application);
        sessionManager = SessionManager.getInstance(application);
        executor = Executors.newFixedThreadPool(2);
    }

    // Getters for LiveData
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getSuccessMessage() {
        return successMessage;
    }

    // Get user orders
    public LiveData<List<Order>> getUserOrders(int userId) {
        return orderRepository.getOrdersByUser(userId);
    }

    public LiveData<List<Order>> getUserOrdersByStatus(int userId, String status) {
        return orderRepository.getUserOrdersByStatus(userId, status);
    }

    // Cancel order
    public void cancelOrder(int orderId) {
        isLoading.postValue(true);

        executor.execute(() -> {
            try {
                // Check if order can be cancelled
                Boolean canCancel = orderRepository.canCancelOrder(orderId).get();

                if (canCancel != null && canCancel) {
                    orderRepository.cancelOrder(orderId);
                    successMessage.postValue("Đơn hàng đã được hủy thành công");
                } else {
                    errorMessage.postValue("Không thể hủy đơn hàng này");
                }
            } catch (Exception e) {
                errorMessage.postValue("Lỗi khi hủy đơn hàng: " + e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        });
    }

    // Reorder items - add all items from an order back to cart
    public void reorderItems(int orderId) {
        isLoading.postValue(true);

        executor.execute(() -> {
            try {
                SessionManager.SessionUser currentUser = sessionManager.getLoggedInUser();
                if (currentUser == null) {
                    errorMessage.postValue("Vui lòng đăng nhập để thực hiện chức năng này");
                    return;
                }

                // Get order items
                List<OrderItem> orderItems = orderItemRepository.getOrderItemsSync(orderId).get();

                if (orderItems != null && !orderItems.isEmpty()) {
                    // Add each item back to cart
                    for (OrderItem item : orderItems) {
                        cartRepository.addToCart(
                                currentUser.getId(),
                                item.getProductId(),
                                item.getQuantity());
                    }

                    successMessage.postValue(String.format("Đã thêm %d sản phẩm vào giỏ hàng", orderItems.size()));
                } else {
                    errorMessage.postValue("Không tìm thấy sản phẩm trong đơn hàng này");
                }
            } catch (Exception e) {
                errorMessage.postValue("Lỗi khi thêm sản phẩm vào giỏ hàng: " + e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        });
    }

    // Get order statistics
    public LiveData<Integer> getUserOrderCount(int userId) {
        return orderRepository.getUserOrderCount(userId);
    }

    public void clearMessages() {
        errorMessage.postValue(null);
        successMessage.postValue(null);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}