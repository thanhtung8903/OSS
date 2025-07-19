package com.example.oss.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.oss.entity.Review;
import com.example.oss.repository.ReviewRepository;
import com.example.oss.util.SessionManager;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReviewViewModel extends AndroidViewModel {

    private ReviewRepository reviewRepository;
    private SessionManager sessionManager;
    private ExecutorService executor;

    private MutableLiveData<String> errorMessage;
    private MutableLiveData<Boolean> isLoading;
    private MutableLiveData<String> successMessage;
    private MutableLiveData<Boolean> canWriteReview = new MutableLiveData<>(false);

    public ReviewViewModel(@NonNull Application application) {
        super(application);
        reviewRepository = new ReviewRepository(application);
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

    public LiveData<Boolean> getCanWriteReview() {
        return canWriteReview;
    }

    // Get reviews for a product
    public LiveData<List<Review>> getReviewsByProduct(int productId) {
        return reviewRepository.getReviewsByProduct(productId);
    }

    // Get average rating for a product
    public LiveData<Double> getAverageRatingForProduct(int productId) {
        return reviewRepository.getAverageRatingForProduct(productId);
    }

    // Get review count for a product
    public LiveData<Integer> getReviewCountForProduct(int productId) {
        return reviewRepository.getReviewCountForProduct(productId);
    }

    // Check if user can write review for a product
    public void checkCanWriteReview(int productId) {
        int userId = getCurrentUserId();
        if (userId == -1) {
            canWriteReview.postValue(false);
            return;
        }

        executor.execute(() -> {
            try {
                // Check if user has purchased this product first
                boolean hasPurchased = reviewRepository.hasUserPurchasedProduct(userId, productId).get();

                if (!hasPurchased) {
                    canWriteReview.postValue(false);
                    errorMessage.postValue("Bạn cần mua sản phẩm trước khi có thể đánh giá");
                    return;
                }

                // Check if user already reviewed this product
                boolean canReview = reviewRepository.canUserReviewProduct(userId, productId).get();
                canWriteReview.postValue(canReview);

                if (!canReview) {
                    errorMessage.postValue("Bạn đã đánh giá sản phẩm này rồi");
                }

            } catch (Exception e) {
                canWriteReview.postValue(false);
                errorMessage.postValue("Lỗi khi kiểm tra quyền đánh giá: " + e.getMessage());
            }
        });
    }

    // Add new review
    public void addReview(int productId, int rating, String comment) {
        int userId = getCurrentUserId();
        if (userId == -1) {
            errorMessage.setValue("Vui lòng đăng nhập để đánh giá sản phẩm");
            return;
        }

        if (!reviewRepository.isValidRating(rating)) {
            errorMessage.setValue("Rating phải từ 1-5 sao");
            return;
        }

        if (!reviewRepository.isValidComment(comment)) {
            errorMessage.setValue("Bình luận phải có ít nhất 10 ký tự");
            return;
        }

        isLoading.setValue(true);
        executor.execute(() -> {
            try {
                // Check if user can review this product
                boolean canReview = reviewRepository.canUserReviewProduct(userId, productId).get();
                if (!canReview) {
                    errorMessage.postValue("Bạn đã đánh giá sản phẩm này rồi");
                    return;
                }

                // Check if user has purchased this product
                boolean hasPurchased = reviewRepository.hasUserPurchasedProduct(userId, productId).get();
                if (!hasPurchased) {
                    errorMessage.postValue("Bạn chỉ có thể đánh giá sản phẩm đã mua");
                    return;
                }

                reviewRepository.addReview(userId, productId, rating, comment);
                successMessage.postValue("Đã thêm đánh giá thành công!");
                canWriteReview.postValue(false); // User can't review again

            } catch (Exception e) {
                errorMessage.postValue("Lỗi khi thêm đánh giá: " + e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        });
    }

    // Update existing review
    public void updateReview(int reviewId, int rating, String comment) {
        if (!reviewRepository.isValidRating(rating)) {
            errorMessage.setValue("Rating phải từ 1-5 sao");
            return;
        }

        if (!reviewRepository.isValidComment(comment)) {
            errorMessage.setValue("Bình luận phải có ít nhất 10 ký tự");
            return;
        }

        isLoading.setValue(true);
        executor.execute(() -> {
            try {
                reviewRepository.updateReview(reviewId, rating, comment);
                successMessage.postValue("Đã cập nhật đánh giá thành công!");
            } catch (Exception e) {
                errorMessage.postValue("Lỗi khi cập nhật đánh giá: " + e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        });
    }

    // Delete review
    public void deleteReview(int reviewId) {
        isLoading.setValue(true);
        executor.execute(() -> {
            try {
                reviewRepository.deleteReviewById(reviewId);
                successMessage.postValue("Đã xóa đánh giá thành công!");
            } catch (Exception e) {
                errorMessage.postValue("Lỗi khi xóa đánh giá: " + e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        });
    }

    // Get user's review for a specific product
    public void getUserReviewForProduct(int productId, MutableLiveData<Review> userReview) {
        int userId = getCurrentUserId();
        if (userId == -1) {
            userReview.postValue(null);
            return;
        }

        executor.execute(() -> {
            try {
                Review review = reviewRepository.getUserReviewForProduct(userId, productId).get();
                userReview.postValue(review);
            } catch (Exception e) {
                userReview.postValue(null);
                errorMessage.postValue("Lỗi khi tải đánh giá: " + e.getMessage());
            }
        });
    }

    // Clear messages
    public void clearSuccess() {
        successMessage.setValue(null);
    }

    public void clearError() {
        errorMessage.setValue(null);
    }
}