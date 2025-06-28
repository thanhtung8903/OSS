package com.example.oss.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.oss.entity.Cart;
import com.example.oss.entity.Order;
import com.example.oss.entity.OrderItem;
import com.example.oss.dao.CartDao;
import com.example.oss.repository.CartRepository;
import com.example.oss.repository.OrderRepository;
import com.example.oss.repository.OrderItemRepository;
import com.example.oss.util.SessionManager;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OrderViewModel extends AndroidViewModel {

    private OrderRepository orderRepository;
    private CartRepository cartRepository;
    private OrderItemRepository orderItemRepository;
    private SessionManager sessionManager;
    private ExecutorService executor;

    private MutableLiveData<OrderCreationResult> orderCreationResult = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public OrderViewModel(@NonNull Application application) {
        super(application);
        orderRepository = new OrderRepository(application);
        cartRepository = new CartRepository(application);
        orderItemRepository = new OrderItemRepository(application);
        sessionManager = SessionManager.getInstance(application);
        executor = Executors.newFixedThreadPool(2);
    }

    // Getters for LiveData
    public LiveData<OrderCreationResult> getOrderCreationResult() {
        return orderCreationResult;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    // Methods for OrderDetailActivity
    public LiveData<Order> getOrderById(int orderId) {
        return orderRepository.getOrderById(orderId);
    }

    public LiveData<List<OrderItem>> getOrderItems(int orderId) {
        return orderItemRepository.getOrderItems(orderId);
    }

    // Main method to create order from cart
    public void createOrderFromCart(int userId, int shippingAddressId, String paymentMethod) {
        isLoading.postValue(true);

        executor.execute(() -> {
            try {
                // 1. Get cart items for user
                List<CartDao.CartWithProduct> cartItems = cartRepository.getCartWithProductsSync(userId);

                if (cartItems == null || cartItems.isEmpty()) {
                    handleError("Giỏ hàng trống");
                    return;
                }

                // 2. Validate stock availability
                if (!validateStockAvailability(cartItems)) {
                    handleError("Một số sản phẩm không đủ hàng trong kho");
                    return;
                }

                // 3. Convert cart items to order items
                List<OrderItemRepository.CartItem> orderCartItems = new ArrayList<>();
                for (CartDao.CartWithProduct cart : cartItems) {
                    orderCartItems.add(new OrderItemRepository.CartItem(
                            cart.getProductId(),
                            cart.getQuantity(),
                            cart.getPrice()));
                }

                // 4. Create order
                long orderId = orderRepository.createOrder(
                        userId,
                        shippingAddressId,
                        convertToOrderItems(cartItems),
                        paymentMethod).get();

                if (orderId > 0) {
                    // 5. Clear cart after successful order creation
                    cartRepository.clearCartSync(userId);

                    // 6. Return success result
                    OrderCreationResult result = new OrderCreationResult(true, (int) orderId, null);
                    orderCreationResult.postValue(result);
                } else {
                    handleError("Không thể tạo đơn hàng");
                }

            } catch (Exception e) {
                handleError("Lỗi: " + e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        });
    }

    private boolean validateStockAvailability(List<CartDao.CartWithProduct> cartItems) {
        // TODO: Implement stock validation
        // For now, assume all items are available
        return true;
    }

    private List<OrderItem> convertToOrderItems(List<CartDao.CartWithProduct> cartItems) {
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartDao.CartWithProduct cart : cartItems) {
            OrderItem orderItem = OrderItem.builder()
                    .productId(cart.getProductId())
                    .quantity(cart.getQuantity())
                    .priceAtPurchase(cart.getPrice())
                    .build();
            orderItems.add(orderItem);
        }
        return orderItems;
    }

    private void handleError(String message) {
        isLoading.postValue(false);
        errorMessage.postValue(message);
        OrderCreationResult result = new OrderCreationResult(false, 0, message);
        orderCreationResult.postValue(result);
    }

    public void clearError() {
        errorMessage.postValue(null);
    }

    // Result class for order creation
    public static class OrderCreationResult {
        private boolean success;
        private int orderId;
        private String errorMessage;

        public OrderCreationResult(boolean success, int orderId, String errorMessage) {
            this.success = success;
            this.orderId = orderId;
            this.errorMessage = errorMessage;
        }

        public boolean isSuccess() {
            return success;
        }

        public int getOrderId() {
            return orderId;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}