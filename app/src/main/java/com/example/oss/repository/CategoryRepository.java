package com.example.oss.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.example.oss.database.AppDatabase;
import com.example.oss.dao.CategoryDao;
import com.example.oss.entity.Category;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CategoryRepository {
    private CategoryDao categoryDao;
    private LiveData<List<Category>> allCategories;
    private LiveData<List<Category>> rootCategories;
    private ExecutorService executor;

    public CategoryRepository(Application application) {
        AppDatabase database = AppDatabase.getDatabase(application);
        categoryDao = database.categoryDao();
        allCategories = categoryDao.getAllCategories();
        rootCategories = categoryDao.getRootCategories();
        executor = Executors.newFixedThreadPool(2);
    }

    // Read operations
    public LiveData<List<Category>> getAllCategories() {
        return allCategories;
    }

    public LiveData<List<Category>> getRootCategories() {
        return rootCategories;
    }

    public LiveData<List<Category>> getSubCategories(int parentId) {
        return categoryDao.getSubCategories(parentId);
    }

    public LiveData<Category> getCategoryById(int id) {
        return categoryDao.getCategoryById(id);
    }

    // Write operations
    public void insertCategory(Category category) {
        executor.execute(() -> categoryDao.insertCategory(category));
    }

    public void updateCategory(Category category) {
        executor.execute(() -> categoryDao.updateCategory(category));
    }

    public void deleteCategory(Category category) {
        executor.execute(() -> categoryDao.deleteCategory(category));
    }

    // Business logic methods
    public void createRootCategory(String name, String description) {
        Category category = new Category(name, description, null);
        insertCategory(category);
    }

    public void createSubCategory(String name, String description, int parentId) {
        Category category = new Category(name, description, parentId);
        insertCategory(category);
    }

    // Methods để kiểm tra category có thể xóa được không
    public boolean canDeleteCategory(int categoryId) {
        try {
            int subCategoryCount = categoryDao.hasSubCategories(categoryId);
            int productCount = categoryDao.hasProducts(categoryId);
            
            // Có thể xóa nếu không có sub-categories và không có products
            return subCategoryCount == 0 && productCount == 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getDeleteErrorMessage(int categoryId) {
        try {
            int subCategoryCount = categoryDao.hasSubCategories(categoryId);
            int productCount = categoryDao.hasProducts(categoryId);
            
            StringBuilder errorMessage = new StringBuilder();
            boolean hasError = false;
            
            if (subCategoryCount > 0) {
                errorMessage.append("🔸 Có ").append(subCategoryCount).append(" danh mục con\n");
                hasError = true;
            }
            
            if (productCount > 0) {
                errorMessage.append("🔸 Có ").append(productCount).append(" sản phẩm\n");
                hasError = true;
            }
            
            if (hasError) {
                errorMessage.append("\n📋 Để xóa được danh mục này, bạn cần:\n");
                if (subCategoryCount > 0) {
                    errorMessage.append("• Xóa tất cả ").append(subCategoryCount).append(" danh mục con trước\n");
                }
                if (productCount > 0) {
                    errorMessage.append("• Xóa hoặc chuyển ").append(productCount).append(" sản phẩm sang danh mục khác\n");
                }
            } else {
                errorMessage.append("❓ Lỗi không xác định");
            }
            
            return errorMessage.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "❌ Lỗi khi kiểm tra thông tin danh mục: " + e.getMessage();
        }
    }

    public int getSubCategoryCount(int categoryId) {
        try {
            return categoryDao.getSubCategoryCount(categoryId);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int getProductCountByCategory(int categoryId) {
        try {
            return categoryDao.getProductCountByCategory(categoryId);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}