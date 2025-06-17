package com.example.oss.dao;

import androidx.room.*;
import androidx.lifecycle.LiveData;
import com.example.oss.entity.Product;
import java.util.List;

@Dao
public interface ProductDao {
    @Query("SELECT * FROM products WHERE is_active = 1 ORDER BY name ASC")
    LiveData<List<Product>> getAllActiveProducts();

    @Query("SELECT * FROM products WHERE category_id = :categoryId AND is_active = 1 ORDER BY name ASC")
    LiveData<List<Product>> getProductsByCategory(int categoryId);

    @Query("SELECT * FROM products WHERE id = :id")
    LiveData<Product> getProductById(int id);

    @Query("SELECT * FROM products WHERE name LIKE '%' || :searchQuery || '%' AND is_active = 1")
    LiveData<List<Product>> searchProducts(String searchQuery);

    @Insert
    long insertProduct(Product product);

    @Update
    void updateProduct(Product product);

    @Delete
    void deleteProduct(Product product);
}