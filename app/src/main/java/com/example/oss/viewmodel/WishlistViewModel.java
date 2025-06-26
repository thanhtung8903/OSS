package com.example.oss.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.oss.entity.Product;
import com.example.oss.entity.Wishlist;
import com.example.oss.repository.WishlistRepository;
import com.example.oss.util.SessionManager;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WishlistViewModel extends AndroidViewModel {

    private WishlistRepository wishlistRepository;
    private SessionManager sessionManager;
    private ExecutorService executor;

    private MutableLiveData<String> errorMessage;
    private MutableLiveData<Boolean> isLoading;
    private MutableLiveData<String> successMessage;
    private MutableLiveData<Boolean> forceRefresh = new MutableLiveData<>(false);

    public WishlistViewModel(@NonNull Application application) {
        super(application);
        wishlistRepository = new WishlistRepository(application);
        sessionManager = SessionManager.getInstance(application);
        executor = Executors.newFixedThreadPool(2);

        errorMessage = new MutableLiveData<>();
        isLoading = new MutableLiveData<>(false);
        successMessage = new MutableLiveData<>();
    }

    // Helper method để get current user ID
    private int getCurrentUserId() {
        SessionManager.SessionUser currentUser = sessionManager.getCurrentUser();
        return currentUser != null ? currentUser.getId() : -1;
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

    public LiveData<List<Product>> getWishlistProducts() {
        int userId = getCurrentUserId();
        if (userId != -1) {
            return wishlistRepository.getWishlistProducts(userId);
        }
        return new MutableLiveData<>();
    }

    public LiveData<Integer> getWishlistCount() {
        int userId = getCurrentUserId();
        if (userId != -1) {
            return wishlistRepository.getWishlistCount(userId);
        }
        return new MutableLiveData<>(0);
    }

    public LiveData<Boolean> isProductInWishlist(int productId) {
        int userId = getCurrentUserId();
        if (userId != -1) {
            return wishlistRepository.isProductInWishlist(userId, productId);
        }
        return new MutableLiveData<>(false);
    }

    // Method để force refresh wishlist (nếu cần thiết)
    public void forceRefreshWishlist() {
        forceRefresh.setValue(true);
    }

    public LiveData<Boolean> getForceRefresh() {
        return forceRefresh;
    }

    public void clearForceRefresh() {
        forceRefresh.setValue(false);
    }

    // Actions
    public void addToWishlist(int productId) {
        int userId = getCurrentUserId();
        if (userId == -1) {
            errorMessage.setValue("Vui lòng đăng nhập để thêm vào danh sách yêu thích");
            return;
        }

        isLoading.setValue(true);
        executor.execute(() -> {
            try {
                wishlistRepository.addToWishlist(userId, productId);
                successMessage.postValue("Đã thêm vào danh sách yêu thích");
            } catch (Exception e) {
                errorMessage.postValue("Lỗi khi thêm vào danh sách yêu thích");
            } finally {
                isLoading.postValue(false);
            }
        });
    }

    public void removeFromWishlist(int productId) {
        int userId = getCurrentUserId();
        if (userId == -1) {
            errorMessage.setValue("Vui lòng đăng nhập");
            return;
        }

        isLoading.setValue(true);
        executor.execute(() -> {
            try {
                wishlistRepository.removeFromWishlist(userId, productId);
                successMessage.postValue("Đã xóa khỏi danh sách yêu thích");
            } catch (Exception e) {
                errorMessage.postValue("Lỗi khi xóa khỏi danh sách yêu thích");
            } finally {
                isLoading.postValue(false);
            }
        });
    }

    public void toggleWishlist(int productId) {
        int userId = getCurrentUserId();
        if (userId == -1) {
            errorMessage.setValue("Vui lòng đăng nhập để sử dụng danh sách yêu thích");
            return;
        }

        isLoading.setValue(true);
        executor.execute(() -> {
            try {
                Boolean wasAdded = wishlistRepository.toggleWishlist(userId, productId).get();
                if (wasAdded != null) {
                    if (wasAdded) {
                        successMessage.postValue("Đã thêm vào danh sách yêu thích");
                    } else {
                        successMessage.postValue("Đã xóa khỏi danh sách yêu thích");
                    }
                }
            } catch (Exception e) {
                errorMessage.postValue("Lỗi khi cập nhật danh sách yêu thích");
            } finally {
                isLoading.postValue(false);
            }
        });
    }

    public void clearWishlist() {
        int userId = getCurrentUserId();
        if (userId == -1) {
            errorMessage.setValue("Vui lòng đăng nhập");
            return;
        }

        isLoading.setValue(true);
        executor.execute(() -> {
            try {
                wishlistRepository.clearWishlist(userId);
                successMessage.postValue("Đã xóa toàn bộ danh sách yêu thích");
            } catch (Exception e) {
                errorMessage.postValue("Lỗi khi xóa danh sách yêu thích");
            } finally {
                isLoading.postValue(false);
            }
        });
    }

    // Clear messages
    public void clearError() {
        errorMessage.setValue(null);
    }

    public void clearSuccess() {
        successMessage.setValue(null);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}