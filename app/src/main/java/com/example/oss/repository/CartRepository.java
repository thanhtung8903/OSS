package com.example.oss.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.example.oss.dao.CartDao;
import com.example.oss.database.AppDatabase;
import com.example.oss.entity.Cart;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class CartRepository {
    private CartDao cartDao;
    private ExecutorService executor;

    public CartRepository(Application application) {
        AppDatabase database = AppDatabase.getDatabase(application);
        cartDao = database.cartDao();
        executor = Executors.newFixedThreadPool(4);
    }

    // Read operations
    public LiveData<List<Cart>> getCartItems(int userId) {
        return cartDao.getCartItems(userId);
    }

    public LiveData<List<CartDao.CartWithProduct>> getCartWithProducts(int userId) {
        return cartDao.getCartWithProducts(userId);
    }

    public LiveData<Integer> getCartCount(int userId) {
        return cartDao.getCartCount(userId);
    }

    public LiveData<Integer> getTotalQuantity(int userId) {
        return cartDao.getTotalQuantity(userId);
    }

    public LiveData<BigDecimal> getCartTotal(int userId) {
        return cartDao.getCartTotal(userId);
    }

    // Write operations
    public void addToCart(int userId, int productId, int quantity) {
        executor.execute(() -> {
            Cart existingItem = cartDao.getCartItem(userId, productId);
            if (existingItem != null) {
                // Update existing item
                existingItem.setQuantity(existingItem.getQuantity() + quantity);
                cartDao.updateCartItem(existingItem);
            } else {
                // Create new item
                Cart newItem = Cart.builder()
                        .userId(userId)
                        .productId(productId)
                        .quantity(quantity)
                        .build();
                cartDao.insertCartItem(newItem);
            }
        });
    }

    public void updateQuantity(int userId, int productId, int newQuantity) {
        executor.execute(() -> {
            if (newQuantity <= 0) {
                cartDao.removeCartItemByIds(userId, productId);
            } else {
                cartDao.updateQuantity(userId, productId, newQuantity);
            }
        });
    }

    public void removeFromCart(int userId, int productId) {
        executor.execute(() -> cartDao.removeCartItemByIds(userId, productId));
    }

    public void clearCart(int userId) {
        executor.execute(() -> cartDao.clearCart(userId));
    }

    // Business logic methods
    public Future<Boolean> isProductInCart(int userId, int productId) {
        return executor.submit(() -> {
            Cart item = cartDao.getCartItem(userId, productId);
            return item != null;
        });
    }

    public Future<Boolean> isCartEmpty(int userId) {
        return executor.submit(() -> {
            int count = cartDao.getCartCountSync(userId);
            return count == 0;
        });
    }

    public Future<CartSummary> getCartSummary(int userId) {
        return executor.submit(() -> {
            int totalItems = cartDao.getCartCountSync(userId);
            int totalQuantity = cartDao.getTotalQuantitySync(userId);
            BigDecimal totalAmount = cartDao.getCartTotalSync(userId);

            return new CartSummary(totalItems, totalQuantity,
                    totalAmount != null ? totalAmount : BigDecimal.ZERO);
        });
    }

    // Helper class
    public static class CartSummary {
        private int totalItems;
        private int totalQuantity;
        private BigDecimal totalAmount;

        public CartSummary(int totalItems, int totalQuantity, BigDecimal totalAmount) {
            this.totalItems = totalItems;
            this.totalQuantity = totalQuantity;
            this.totalAmount = totalAmount;
        }

        // Getters
        public int getTotalItems() {
            return totalItems;
        }

        public int getTotalQuantity() {
            return totalQuantity;
        }

        public BigDecimal getTotalAmount() {
            return totalAmount;
        }
    }
}