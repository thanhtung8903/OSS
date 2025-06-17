package com.example.oss.dao;

import androidx.room.*;
import androidx.lifecycle.LiveData;
import com.example.oss.entity.Wishlist;
import com.example.oss.entity.Product;
import java.util.List;

@Dao
public interface WishlistDao {
    @Query("SELECT p.* FROM products p " +
            "INNER JOIN wishlist w ON p.id = w.product_id " +
            "WHERE w.user_id = :userId " +
            "ORDER BY w.added_at DESC")
    LiveData<List<Product>> getWishlistProducts(int userId);

    @Query("SELECT * FROM wishlist WHERE user_id = :userId ORDER BY added_at DESC")
    LiveData<List<Wishlist>> getWishlistByUser(int userId);

    @Query("SELECT * FROM wishlist WHERE user_id = :userId AND product_id = :productId LIMIT 1")
    Wishlist getWishlistItem(int userId, int productId);

    @Query("SELECT COUNT(*) FROM wishlist WHERE user_id = :userId")
    LiveData<Integer> getWishlistCount(int userId);

    @Query("SELECT EXISTS(SELECT 1 FROM wishlist WHERE user_id = :userId AND product_id = :productId)")
    LiveData<Boolean> isProductInWishlist(int userId, int productId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addToWishlist(Wishlist wishlist);

    @Delete
    void removeFromWishlist(Wishlist wishlist);

    @Query("DELETE FROM wishlist WHERE user_id = :userId AND product_id = :productId")
    void removeFromWishlist(int userId, int productId);

    @Query("DELETE FROM wishlist WHERE user_id = :userId")
    void clearWishlist(int userId);
}