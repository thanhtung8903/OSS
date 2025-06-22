package com.example.oss.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.LiveData;
import com.example.oss.repository.ProductRepository;
import com.example.oss.repository.CategoryRepository;
import com.example.oss.entity.Product;
import com.example.oss.entity.Category;
import java.util.List;

public class ProductViewModel extends AndroidViewModel {

    private ProductRepository productRepository;
    private CategoryRepository categoryRepository;

    // LiveData for UI
    private MutableLiveData<Boolean> isLoading;
    private MutableLiveData<String> errorMessage;

    public ProductViewModel(Application application) {
        super(application);
        productRepository = new ProductRepository(application);
        categoryRepository = new CategoryRepository(application);

        // Initialize LiveData
        isLoading = new MutableLiveData<>(false);
        errorMessage = new MutableLiveData<>();
    }

    // Getters for LiveData
    public LiveData<List<Product>> getAllProducts() {
        return productRepository.getAllProducts();
    }

    public LiveData<List<Product>> getProductsByCategory(int categoryId) {
        return productRepository.getProductsByCategory(categoryId);
    }

    public LiveData<Product> getProductById(int productId) {
        return productRepository.getProductById(productId);
    }

    public LiveData<List<Category>> getAllCategories() {
        return categoryRepository.getAllCategories();
    }

    public LiveData<List<Product>> searchProducts(String query) {
        return productRepository.searchProducts(query);
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    // Insert sample data
    public void insertSampleData() {
        isLoading.postValue(true);
        categoryRepository.insertSampleData();
        productRepository.insertSampleData();
        isLoading.postValue(false);
    }

    // Clear error message
    public void clearError() {
        errorMessage.postValue(null);
    }
}