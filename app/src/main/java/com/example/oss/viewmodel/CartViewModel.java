package com.example.oss.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.oss.dao.CartDao;
import com.example.oss.repository.CartRepository;
import com.example.oss.util.SessionManager;
import java.math.BigDecimal;
import java.util.List;

public class CartViewModel extends AndroidViewModel {

    private CartRepository cartRepository;
    private SessionManager sessionManager;
    private MutableLiveData<String> errorMessage;
    private MutableLiveData<Boolean> isLoading;

    public CartViewModel(@NonNull Application application) {
        super(application);
        cartRepository = new CartRepository(application);
        sessionManager = SessionManager.getInstance(application);
        errorMessage = new MutableLiveData<>();
        isLoading = new MutableLiveData<>(false);
    }

    // Helper method để get current user
    private SessionManager.SessionUser getCurrentUser() {
        return sessionManager.getCurrentUser();
    }

    // Helper method để get current user ID
    private int getCurrentUserId() {
        SessionManager.SessionUser currentUser = getCurrentUser();
        return currentUser != null ? currentUser.getId() : -1;
    }

    // Getters for LiveData
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<List<CartDao.CartWithProduct>> getCartItems() {
        int userId = getCurrentUserId();
        if (userId != -1) {
            return cartRepository.getCartWithProducts(userId);
        }
        return new MutableLiveData<>();
    }

    public LiveData<Integer> getCartCount() {
        int userId = getCurrentUserId();
        if (userId != -1) {
            return cartRepository.getCartCount(userId);
        }
        return new MutableLiveData<>(0);
    }

    public LiveData<Integer> getTotalQuantity() {
        int userId = getCurrentUserId();
        if (userId != -1) {
            return cartRepository.getTotalQuantity(userId);
        }
        return new MutableLiveData<>(0);
    }

    public LiveData<BigDecimal> getCartTotal() {
        int userId = getCurrentUserId();
        if (userId != -1) {
            return cartRepository.getCartTotal(userId);
        }
        return new MutableLiveData<>(BigDecimal.ZERO);
    }

    // Cart operations
    public void addToCart(int productId, int quantity) {
        SessionManager.SessionUser currentUser = getCurrentUser();
        if (currentUser == null) {
            errorMessage.setValue("Vui lòng đăng nhập để thêm vào giỏ hàng");
            return;
        }

        isLoading.setValue(true);
        try {
            cartRepository.addToCart(currentUser.getId(), productId, quantity);
        } catch (Exception e) {
            errorMessage.setValue("Lỗi khi thêm vào giỏ hàng: " + e.getMessage());
        } finally {
            isLoading.setValue(false);
        }
    }

    public void updateQuantity(int productId, int newQuantity) {
        SessionManager.SessionUser currentUser = getCurrentUser();
        if (currentUser == null) {
            errorMessage.setValue("Vui lòng đăng nhập");
            return;
        }

        try {
            cartRepository.updateQuantity(currentUser.getId(), productId, newQuantity);
        } catch (Exception e) {
            errorMessage.setValue("Lỗi khi cập nhật số lượng: " + e.getMessage());
        }
    }

    public void removeFromCart(int productId) {
        SessionManager.SessionUser currentUser = getCurrentUser();
        if (currentUser == null) {
            errorMessage.setValue("Vui lòng đăng nhập");
            return;
        }

        try {
            cartRepository.removeFromCart(currentUser.getId(), productId);
        } catch (Exception e) {
            errorMessage.setValue("Lỗi khi xóa khỏi giỏ hàng: " + e.getMessage());
        }
    }

    public void clearCart() {
        SessionManager.SessionUser currentUser = getCurrentUser();
        if (currentUser == null) {
            errorMessage.setValue("Vui lòng đăng nhập");
            return;
        }

        try {
            cartRepository.clearCart(currentUser.getId());
        } catch (Exception e) {
            errorMessage.setValue("Lỗi khi xóa giỏ hàng: " + e.getMessage());
        }
    }

    public void clearError() {
        errorMessage.setValue(null);
    }
}