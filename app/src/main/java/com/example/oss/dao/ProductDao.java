package com.example.oss.dao;

import androidx.room.*;
import androidx.lifecycle.LiveData;
import com.example.oss.entity.Product;
import java.math.BigDecimal;
import java.util.List;

@Dao
public interface ProductDao {

        @Query("SELECT * FROM products WHERE is_active = 1 ORDER BY name ASC")
        LiveData<List<Product>> getAllActiveProducts();

        @Query("SELECT * FROM products ORDER BY name ASC")
        LiveData<List<Product>> getAllProducts();
  
        @Query("SELECT * FROM products WHERE category_id = :categoryId AND is_active = 1 ORDER BY name ASC")
        LiveData<List<Product>> getProductsByCategory(int categoryId);

        @Query("SELECT * FROM products WHERE id = :id")
        LiveData<Product> getProductById(int id);

      @Query("SELECT * FROM products WHERE " +
            "LOWER(name) LIKE LOWER('%' || :searchQuery || '%') " +
            "ORDER BY name ASC")
    LiveData<List<Product>> searchAllProducts(String searchQuery);

    @Query("SELECT * FROM products WHERE " +
            "category_id = :categoryId " +
            "ORDER BY name ASC")
    LiveData<List<Product>> getAllProductsByCategory(int categoryId);

    @Query("SELECT * FROM products WHERE " +
            "LOWER(name) LIKE LOWER('%' || :searchQuery || '%') " +
            "AND category_id = :categoryId " +
            "ORDER BY name ASC")
    LiveData<List<Product>> searchAllProductsByCategory(String searchQuery, int categoryId);

  
        @Query("SELECT * FROM products WHERE " +
                        "(LOWER(name) LIKE LOWER('%' || :searchQuery || '%') OR " +
                        " name LIKE '%' || :searchQuery || '%') " +
                        "AND is_active = 1 " +
                        "ORDER BY name ASC")
        LiveData<List<Product>> searchProducts(String searchQuery);

        @Query("SELECT * FROM products WHERE " +
                        "(name LIKE '%' || :searchQuery || '%' OR description LIKE '%' || :searchQuery || '%') " +
                        "AND is_active = 1 " +
                        "AND (:categoryIds IS NULL OR category_id IN (:categoryIds)) " +
                        "ORDER BY name ASC")
        LiveData<List<Product>> searchProductsWithCategories(String searchQuery, List<Integer> categoryIds);

        @Query("SELECT * FROM products WHERE " +
                        "is_active = 1 " +
                        "AND (:categoryIds IS NULL OR category_id IN (:categoryIds)) " +
                        "ORDER BY name ASC")
        LiveData<List<Product>> getProductsByCategories(List<Integer> categoryIds);

        @Query("SELECT * FROM products WHERE " +
                        "(:searchQuery = '' OR " +
                        " name LIKE '%' || :searchQuery || '%' OR " +
                        " description LIKE '%' || :searchQuery || '%' OR " +
                        " LOWER(name) LIKE LOWER('%' || :searchQuery || '%') OR " +
                        " LOWER(description) LIKE LOWER('%' || :searchQuery || '%')) " +
                        "AND is_active = 1 " +
                        "AND (:categoryIds IS NULL OR category_id IN (:categoryIds)) " +
                        "AND price >= :minPrice AND price <= :maxPrice " +
                        "AND (:inStockOnly = 0 OR stock_quantity > 0) " +
                        "ORDER BY " +
                        "CASE WHEN :sortBy = 'price_asc' THEN price END ASC, " +
                        "CASE WHEN :sortBy = 'price_desc' THEN price END DESC, " +
                        "CASE WHEN :sortBy = 'name_asc' THEN LOWER(name) END ASC, " +
                        "CASE WHEN :sortBy = 'name_desc' THEN LOWER(name) END DESC, " +
                        "CASE WHEN :sortBy = 'stock_first' AND stock_quantity > 0 THEN 0 ELSE 1 END ASC, " +
                        "CASE WHEN :sortBy = 'newest_first' THEN id END DESC, " +
                        "LOWER(name) ASC")
        LiveData<List<Product>> searchProductsAdvanced(
                        String searchQuery,
                        List<Integer> categoryIds,
                        BigDecimal minPrice,
                        BigDecimal maxPrice,
                        boolean inStockOnly,
                        String sortBy);

        @Query("SELECT MIN(price) FROM products WHERE is_active = 1")
        LiveData<BigDecimal> getMinPrice();

        @Query("SELECT MAX(price) FROM products WHERE is_active = 1")
        LiveData<BigDecimal> getMaxPrice();

        @Query("SELECT MIN(price) FROM products WHERE " +
                        "(name LIKE '%' || :searchQuery || '%' OR description LIKE '%' || :searchQuery || '%') "
                        +
                        "AND is_active = 1 " +
                        "AND (:categoryIds IS NULL OR category_id IN (:categoryIds))")
        BigDecimal getMinPriceForFilter(String searchQuery, List<Integer> categoryIds);

        @Query("SELECT MAX(price) FROM products WHERE " +
                        "(name LIKE '%' || :searchQuery || '%' OR description LIKE '%' || :searchQuery || '%') "
                        +
                        "AND is_active = 1 " +
                        "AND (:categoryIds IS NULL OR category_id IN (:categoryIds))")
        BigDecimal getMaxPriceForFilter(String searchQuery, List<Integer> categoryIds);

    @Query("SELECT SUM(stock_quantity) FROM products WHERE is_active = 1")
    int getTotalStockQuantity();

    
        @Insert
        long insertProduct(Product product);

        @Update
        void updateProduct(Product product);

        @Delete
        void deleteProduct(Product product);

        @Query("DELETE FROM products")
        void deleteAllProducts();

        @Query("SELECT * FROM products WHERE " +
                        "(LOWER(name) LIKE LOWER('%' || :searchQuery || '%') OR " +
                        " name LIKE '%' || :searchQuery || '%') " +
                        "AND is_active = 1 " +
                        "ORDER BY name ASC")
        List<Product> searchProductsSync(String searchQuery);

        @Query("SELECT * FROM products WHERE is_active = 1")
        List<Product> getAllActiveProductsSync();

        @Query("SELECT COUNT(*) FROM products WHERE is_active = 1")
        int getActiveProductCount();

        @Query("SELECT COUNT(*) FROM products")
        int getTotalProductCount();

        @Query("SELECT * FROM products WHERE " +
                        "LOWER(name) LIKE LOWER('%' || :searchQuery || '%') " +
                        "AND is_active = 1 LIMIT 5")
        List<Product> debugSearchSync(String searchQuery);

        @Query("SELECT * FROM products WHERE " +
                        "(:searchQuery = '' OR " +
                        " name LIKE '%' || :searchQuery || '%' OR " +
                        " description LIKE '%' || :searchQuery || '%' OR " +
                        " LOWER(name) LIKE LOWER('%' || :searchQuery || '%') OR " +
                        " LOWER(description) LIKE LOWER('%' || :searchQuery || '%')) " +
                        "AND is_active = 1 LIMIT 10")
        List<Product> debugAdvancedSearchSimpleSync(String searchQuery);

        @Query("SELECT * FROM products WHERE " +
                        "(:searchQuery = '' OR " +
                        " name LIKE '%' || :searchQuery || '%' OR " +
                        " description LIKE '%' || :searchQuery || '%' OR " +
                        " LOWER(name) LIKE LOWER('%' || :searchQuery || '%') OR " +
                        " LOWER(description) LIKE LOWER('%' || :searchQuery || '%')) " +
                        "AND is_active = 1 " +
                        "AND price >= 0 AND price <= 999999999 LIMIT 10")
        List<Product> debugAdvancedSearchWithPriceSync(String searchQuery);
}