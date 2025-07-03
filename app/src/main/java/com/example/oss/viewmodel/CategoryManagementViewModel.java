package com.example.oss.viewmodel;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.oss.entity.Category;
import com.example.oss.repository.CategoryRepository;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CategoryManagementViewModel extends AndroidViewModel {
    private CategoryRepository repository;
    private LiveData<List<Category>> allCategories;
    private LiveData<List<Category>> rootCategories;
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private ExecutorService executor;
    private Handler mainHandler;

    public interface DeleteCategoryCallback {
        void onResult(boolean canDelete, String errorMessage);
    }

    public CategoryManagementViewModel(Application application) {
        super(application);
        repository = new CategoryRepository(application);
        allCategories = repository.getAllCategories();
        rootCategories = repository.getRootCategories();
        executor = Executors.newFixedThreadPool(2);
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public LiveData<List<Category>> getAllCategories() {
        return allCategories;
    }

    public LiveData<List<Category>> getRootCategories() {
        return rootCategories;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void insertCategory(Category category) {
        isLoading.setValue(true);
        repository.insertCategory(category);
        isLoading.setValue(false);
    }

    public void updateCategory(Category category) {
        isLoading.setValue(true);
        repository.updateCategory(category);
        isLoading.setValue(false);
    }

    public void deleteCategory(Category category) {
        isLoading.setValue(true);
        repository.deleteCategory(category);
        isLoading.setValue(false);
    }

    public void createRootCategory(String name, String description) {
        isLoading.setValue(true);
        repository.createRootCategory(name, description);
        isLoading.setValue(false);
    }

    public void createSubCategory(String name, String description, int parentId) {
        isLoading.setValue(true);
        repository.createSubCategory(name, description, parentId);
        isLoading.setValue(false);
    }

    public void refreshData() {
        // Trigger refresh by updating the LiveData
        // This will cause the UI to refresh
    }

    // Method để kiểm tra category có thể xóa được không (chạy trên background thread)
    public void checkCanDeleteCategory(int categoryId, DeleteCategoryCallback callback) {
        executor.execute(() -> {
            try {
                boolean canDelete = repository.canDeleteCategory(categoryId);
                String errorMessage = repository.getDeleteErrorMessage(categoryId);
                
                // Trả kết quả về UI thread
                mainHandler.post(() -> {
                    callback.onResult(canDelete, errorMessage);
                });
            } catch (Exception e) {
                e.printStackTrace();
                // Trả lỗi về UI thread
                mainHandler.post(() -> {
                    callback.onResult(false, "Lỗi khi kiểm tra: " + e.getMessage());
                });
            }
        });
    }

    // Các method cũ (giữ lại để tương thích)
    public boolean canDeleteCategory(int categoryId) {
        return repository.canDeleteCategory(categoryId);
    }

    public String getDeleteErrorMessage(int categoryId) {
        return repository.getDeleteErrorMessage(categoryId);
    }

    public int getSubCategoryCount(int categoryId) {
        return repository.getSubCategoryCount(categoryId);
    }

    public int getProductCountByCategory(int categoryId) {
        return repository.getProductCountByCategory(categoryId);
    }
} 