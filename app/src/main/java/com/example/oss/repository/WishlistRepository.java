package com.example.oss.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.example.oss.database.AppDatabase;
import com.example.oss.dao.WishlistDao;
import com.example.oss.entity.Wishlist;
import com.example.oss.entity.Product;
import java.util.List;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class WishlistRepository {
    private WishlistDao wishlistDao;
    private ExecutorService executor;

    public WishlistRepository(Application application) {
        AppDatabase database = AppDatabase.getDatabase(application);
        wishlistDao = database.wishlistDao();
        executor = Executors.newFixedThreadPool(2);
    }

    // Read operations
    public LiveData<List<Product>> getWishlistProducts(int userId) {
        return wishlistDao.getWishlistProducts(userId);
    }

    public LiveData<List<Wishlist>> getWishlistByUser(int userId) {
        return wishlistDao.getWishlistByUser(userId);
    }

    public LiveData<Integer> getWishlistCount(int userId) {
        return wishlistDao.getWishlistCount(userId);
    }

    public LiveData<Boolean> isProductInWishlist(int userId, int productId) {
        return wishlistDao.isProductInWishlist(userId, productId);
    }

    public Future<Wishlist> getWishlistItem(int userId, int productId) {
        return executor.submit(() -> wishlistDao.getWishlistItem(userId, productId));
    }

    // Write operations
    public void addToWishlist(int userId, int productId) {
        executor.execute(() -> {
            Wishlist wishlist = new Wishlist(userId, productId);
            wishlist.setAddedAt(new Date());
            wishlistDao.addToWishlist(wishlist);
        });
    }

    public void removeFromWishlist(int userId, int productId) {
        executor.execute(() -> wishlistDao.removeFromWishlist(userId, productId));
    }

    public void removeFromWishlist(Wishlist wishlist) {
        executor.execute(() -> wishlistDao.removeFromWishlist(wishlist));
    }

    public void clearWishlist(int userId) {
        executor.execute(() -> wishlistDao.clearWishlist(userId));
    }

    // Business logic methods
    public Future<Boolean> toggleWishlist(int userId, int productId) {
        return executor.submit(() -> {
            Wishlist existingItem = wishlistDao.getWishlistItem(userId, productId);
            if (existingItem != null) {
                // Remove from wishlist
                wishlistDao.removeFromWishlist(existingItem);
                return false; // Removed
            } else {
                // Add to wishlist
                Wishlist newItem = new Wishlist(userId, productId);
                newItem.setAddedAt(new Date());
                wishlistDao.addToWishlist(newItem);
                return true; // Added
            }
        });
    }

    public Future<Boolean> isWishlistEmpty(int userId) {
        return executor.submit(() -> {
            Integer count = wishlistDao.getWishlistCount(userId).getValue();
            return count == null || count == 0;
        });
    }

    public void moveAllToCart(int userId) {
        executor.execute(() -> {
            // TODO: Implement logic to move all wishlist items to cart
            // This would require integration with cart/order system

            // For now, just clear the wishlist
            clearWishlist(userId);
        });
    }

    // Utility methods
    public Future<List<Integer>> getWishlistProductIds(int userId) {
        return executor.submit(() -> {
            List<Wishlist> wishlistItems = wishlistDao.getWishlistByUser(userId).getValue();
            if (wishlistItems != null) {
                return wishlistItems.stream()
                        .map(Wishlist::getProductId)
                        .collect(java.util.stream.Collectors.toList());
            }
            return new java.util.ArrayList<>();
        });
    }
}