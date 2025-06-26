package com.example.oss.dao;

import androidx.room.*;
import androidx.lifecycle.LiveData;
import com.example.oss.entity.Category;
import java.util.List;

@Dao
public interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY name ASC")
    LiveData<List<Category>> getAllCategories();

    @Query("SELECT * FROM categories WHERE parent_id IS NULL ORDER BY name ASC")
    LiveData<List<Category>> getRootCategories();

    @Query("SELECT * FROM categories WHERE parent_id = :parentId ORDER BY name ASC")
    LiveData<List<Category>> getSubCategories(int parentId);

    @Query("SELECT * FROM categories WHERE id = :id")
    LiveData<Category> getCategoryById(int id);

    @Insert
    long insertCategory(Category category);

    @Update
    void updateCategory(Category category);

    @Delete
    void deleteCategory(Category category);

    // Methods cho sample data
    @Query("DELETE FROM categories")
    void deleteAllCategories();
}