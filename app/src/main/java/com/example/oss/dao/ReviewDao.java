package com.example.oss.dao;

import androidx.room.*;
import androidx.lifecycle.LiveData;
import com.example.oss.entity.Review;
import java.util.List;

@Dao
public interface ReviewDao {
    @Query("SELECT * FROM reviews ORDER BY created_at DESC")
    LiveData<List<Review>> getAllReviews();

    @Query("SELECT * FROM reviews WHERE product_id = :productId ORDER BY created_at DESC")
    LiveData<List<Review>> getReviewsByProduct(int productId);

    @Query("SELECT * FROM reviews WHERE user_id = :userId ORDER BY created_at DESC")
    LiveData<List<Review>> getReviewsByUser(int userId);

    @Query("SELECT * FROM reviews WHERE id = :id")
    LiveData<Review> getReviewById(int id);

    @Query("SELECT AVG(rating) FROM reviews WHERE product_id = :productId")
    LiveData<Double> getAverageRatingForProduct(int productId);

    @Query("SELECT COUNT(*) FROM reviews WHERE product_id = :productId")
    LiveData<Integer> getReviewCountForProduct(int productId);

    @Query("SELECT * FROM reviews WHERE user_id = :userId AND product_id = :productId LIMIT 1")
    Review getUserReviewForProduct(int userId, int productId);

    @Insert
    long insertReview(Review review);

    @Update
    void updateReview(Review review);

    @Delete
    void deleteReview(Review review);

    @Query("DELETE FROM reviews WHERE id = :reviewId")
    void deleteReviewById(int reviewId);

    @Query("DELETE FROM reviews")
    void deleteAll();
}