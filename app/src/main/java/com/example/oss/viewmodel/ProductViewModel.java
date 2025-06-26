package com.example.oss.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.LiveData;
import com.example.oss.repository.ProductRepository;
import com.example.oss.repository.CategoryRepository;
import com.example.oss.entity.Product;
import com.example.oss.entity.Category;
import com.example.oss.util.SearchFilter;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProductViewModel extends AndroidViewModel {

    private ProductRepository productRepository;
    private CategoryRepository categoryRepository;
    private ExecutorService executor;

    // LiveData for UI
    private MutableLiveData<Boolean> isLoading;
    private MutableLiveData<String> errorMessage;
    private MutableLiveData<SearchFilter.FilterState> currentFilter;

    public ProductViewModel(Application application) {
        super(application);
        productRepository = new ProductRepository(application);
        categoryRepository = new CategoryRepository(application);
        executor = Executors.newFixedThreadPool(2);

        // Initialize LiveData
        isLoading = new MutableLiveData<>(false);
        errorMessage = new MutableLiveData<>();
        currentFilter = new MutableLiveData<>(new SearchFilter.FilterState());
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

    // Search methods (existing - for backward compatibility)
    public LiveData<List<Product>> searchProducts(String query) {
        return productRepository.searchProducts(query);
    }

    public LiveData<List<Product>> searchProductsWithCategory(String query, int categoryId) {
        return productRepository.searchProductsWithCategory(query, categoryId);
    }

    public LiveData<List<Product>> searchProductsWithCategories(String query, List<Integer> categoryIds) {
        return productRepository.searchProductsWithCategories(query, categoryIds);
    }

    public LiveData<List<Product>> getProductsByCategories(List<Integer> categoryIds) {
        return productRepository.getProductsByCategories(categoryIds);
    }

    // Advanced search với full filter support
    public LiveData<List<Product>> searchProductsAdvanced(SearchFilter.FilterState filterState) {
        return productRepository.searchProductsAdvanced(filterState);
    }

    // Filter state management
    public LiveData<SearchFilter.FilterState> getCurrentFilter() {
        return currentFilter;
    }

    public void updateFilter(SearchFilter.FilterState filterState) {
        currentFilter.setValue(filterState);
    }

    // Price range methods
    public LiveData<BigDecimal> getMinPrice() {
        return productRepository.getMinPrice();
    }

    public LiveData<BigDecimal> getMaxPrice() {
        return productRepository.getMaxPrice();
    }

    public void getPriceRangeForFilter(String searchQuery, List<Integer> categoryIds,
            PriceRangeCallback callback) {
        executor.execute(() -> {
            try {
                SearchFilter.PriceRange priceRange = productRepository
                        .getPriceRangeForFilter(searchQuery, categoryIds).get();
                callback.onSuccess(priceRange);
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }

    public interface PriceRangeCallback {
        void onSuccess(SearchFilter.PriceRange priceRange);

        void onError(String error);
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    // Clear error message
    public void clearError() {
        errorMessage.setValue(null);
    }

    // Insert sample data
    public void insertSampleData() {
        isLoading.postValue(true);
        isLoading.postValue(false);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}