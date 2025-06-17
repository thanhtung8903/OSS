package com.example.oss.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.example.oss.database.AppDatabase;
import com.example.oss.dao.ReviewDao;
import com.example.oss.entity.Review;
import java.util.List;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ReviewRepository {
    private ReviewDao reviewDao;
    private LiveData<List<Review>> allReviews;
    private ExecutorService executor;

    public ReviewRepository(Application application) {
        AppDatabase database = AppDatabase.getDatabase(application);
        reviewDao = database.reviewDao();
        allReviews = reviewDao.getAllReviews();
        executor = Executors.newFixedThreadPool(2);
    }

    // Read operations
    public LiveData<List<Review>> getAllReviews() {
        return allReviews;
    }

    public LiveData<List<Review>> getReviewsByProduct(int productId) {
        return reviewDao.getReviewsByProduct(productId);
    }

    public LiveData<List<Review>> getReviewsByUser(int userId) {
        return reviewDao.getReviewsByUser(userId);
    }

    public LiveData<Review> getReviewById(int id) {
        return reviewDao.getReviewById(id);
    }

    public LiveData<Double> getAverageRatingForProduct(int productId) {
        return reviewDao.getAverageRatingForProduct(productId);
    }

    public LiveData<Integer> getReviewCountForProduct(int productId) {
        return reviewDao.getReviewCountForProduct(productId);
    }

    public Future<Review> getUserReviewForProduct(int userId, int productId) {
        return executor.submit(() -> reviewDao.getUserReviewForProduct(userId, productId));
    }

    // Write operations
    public void insertReview(Review review) {
        executor.execute(() -> reviewDao.insertReview(review));
    }

    public void updateReview(Review review) {
        executor.execute(() -> reviewDao.updateReview(review));
    }

    public void deleteReview(Review review) {
        executor.execute(() -> reviewDao.deleteReview(review));
    }

    public void deleteReviewById(int reviewId) {
        executor.execute(() -> reviewDao.deleteReviewById(reviewId));
    }

    // Business logic methods
    public void addReview(int userId, int productId, int rating, String comment) {
        Review review = new Review(userId, productId, rating, comment);
        review.setCreatedAt(new Date());
        insertReview(review);
    }

    public Future<Boolean> canUserReviewProduct(int userId, int productId) {
        return executor.submit(() -> {
            Review existingReview = reviewDao.getUserReviewForProduct(userId, productId);
            return existingReview == null; // User chỉ có thể review 1 lần cho mỗi sản phẩm
        });
    }

    public void updateReview(int reviewId, int rating, String comment) {
        executor.execute(() -> {
            Review review = reviewDao.getReviewById(reviewId).getValue();
            if (review != null) {
                review.setRating(rating);
                review.setComment(comment);
                updateReview(review);
            }
        });
    }

    public Future<Boolean> hasUserPurchasedProduct(int userId, int productId) {
        return executor.submit(() -> {
            // TODO: Implement logic to check if user has purchased this product
            // This would require checking OrderItem table
            return true; // Placeholder - assume user has purchased
        });
    }

    // Validation methods
    public boolean isValidRating(int rating) {
        return rating >= 1 && rating <= 5;
    }

    public boolean isValidComment(String comment) {
        return comment != null && comment.trim().length() >= 10; // Minimum 10 characters
    }
}