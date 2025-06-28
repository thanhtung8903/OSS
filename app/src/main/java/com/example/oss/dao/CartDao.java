package com.example.oss.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.example.oss.entity.Cart;
import com.example.oss.entity.Product;
import java.math.BigDecimal;
import java.util.List;

@Dao
public interface CartDao {

    // Read operations
    @Query("SELECT * FROM cart WHERE user_id = :userId ORDER BY added_at DESC")
    LiveData<List<Cart>> getCartItems(int userId);

    @Query("SELECT * FROM cart WHERE user_id = :userId ORDER BY added_at DESC")
    List<Cart> getCartItemsSync(int userId);

    @Query("SELECT * FROM cart WHERE user_id = :userId AND product_id = :productId")
    Cart getCartItem(int userId, int productId);

    @Query("SELECT * FROM cart WHERE user_id = :userId AND product_id = :productId")
    LiveData<Cart> getCartItemLive(int userId, int productId);

    @Query("SELECT COUNT(*) FROM cart WHERE user_id = :userId")
    LiveData<Integer> getCartCount(int userId);

    @Query("SELECT COUNT(*) FROM cart WHERE user_id = :userId")
    int getCartCountSync(int userId);

    @Query("SELECT SUM(quantity) FROM cart WHERE user_id = :userId")
    LiveData<Integer> getTotalQuantity(int userId);

    @Query("SELECT SUM(quantity) FROM cart WHERE user_id = :userId")
    int getTotalQuantitySync(int userId);

    // Lấy thông tin cart với product details - FIX: sử dụng aliases để match field
    // names
    @Query("SELECT c.user_id, c.product_id, c.quantity, c.added_at, " +
            "p.name as product_name, p.price, p.image_url, p.stock_quantity " +
            "FROM cart c " +
            "INNER JOIN products p ON c.product_id = p.id " +
            "WHERE c.user_id = :userId " +
            "ORDER BY c.added_at DESC")
    LiveData<List<CartWithProduct>> getCartWithProducts(int userId);

    @Query("SELECT c.user_id, c.product_id, c.quantity, c.added_at, " +
            "p.name as product_name, p.price, p.image_url, p.stock_quantity " +
            "FROM cart c " +
            "INNER JOIN products p ON c.product_id = p.id " +
            "WHERE c.user_id = :userId " +
            "ORDER BY c.added_at DESC")
    List<CartWithProduct> getCartWithProductsSync(int userId);

    // Tính tổng tiền giỏ hàng
    @Query("SELECT SUM(c.quantity * p.price) " +
            "FROM cart c " +
            "INNER JOIN products p ON c.product_id = p.id " +
            "WHERE c.user_id = :userId")
    LiveData<BigDecimal> getCartTotal(int userId);

    @Query("SELECT SUM(c.quantity * p.price) " +
            "FROM cart c " +
            "INNER JOIN products p ON c.product_id = p.id " +
            "WHERE c.user_id = :userId")
    BigDecimal getCartTotalSync(int userId);

    // Write operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCartItem(Cart cart);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCartItems(List<Cart> carts);

    @Update
    void updateCartItem(Cart cart);

    @Delete
    void removeCartItem(Cart cart);

    @Query("DELETE FROM cart WHERE user_id = :userId AND product_id = :productId")
    void removeCartItemByIds(int userId, int productId);

    @Query("DELETE FROM cart WHERE user_id = :userId")
    void clearCart(int userId);

    @Query("DELETE FROM cart WHERE user_id = :userId")
    void clearCartByUser(int userId);

    @Query("UPDATE cart SET quantity = :quantity WHERE user_id = :userId AND product_id = :productId")
    void updateQuantity(int userId, int productId, int quantity);

    // Data class cho cart với product details - FIX: đổi field names để match với
    // column names
    class CartWithProduct {
        public int user_id; // Đổi từ userId thành user_id
        public int product_id; // Đổi từ productId thành product_id
        public int quantity;
        public java.util.Date added_at; // Đổi từ addedAt thành added_at
        public String product_name;
        public BigDecimal price;
        public String image_url;
        public int stock_quantity;

        // Constructor
        public CartWithProduct() {
        }

        // Getters and setters - UPDATE: đổi tên methods để match với new field names
        public int getUserId() {
            return user_id;
        }

        public void setUserId(int user_id) {
            this.user_id = user_id;
        }

        public int getProductId() {
            return product_id;
        }

        public void setProductId(int product_id) {
            this.product_id = product_id;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public java.util.Date getAddedAt() {
            return added_at;
        }

        public void setAddedAt(java.util.Date added_at) {
            this.added_at = added_at;
        }

        public String getProductName() {
            return product_name;
        }

        public void setProductName(String product_name) {
            this.product_name = product_name;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        public String getImageUrl() {
            return image_url;
        }

        public void setImageUrl(String image_url) {
            this.image_url = image_url;
        }

        public int getStockQuantity() {
            return stock_quantity;
        }

        public void setStockQuantity(int stock_quantity) {
            this.stock_quantity = stock_quantity;
        }

        // Calculated fields
        public BigDecimal getTotalPrice() {
            return price != null ? price.multiply(BigDecimal.valueOf(quantity)) : BigDecimal.ZERO;
        }
    }
}