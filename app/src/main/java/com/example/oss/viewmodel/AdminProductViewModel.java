package com.example.oss.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.oss.entity.Product;
import com.example.oss.repository.AdminProductRepository;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdminProductViewModel extends AndroidViewModel {
    private final AdminProductRepository adminProductRepository;
    private final ExecutorService executor;
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public AdminProductViewModel(@NonNull Application application) {
        super(application);
        adminProductRepository = new AdminProductRepository(application);
        executor = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Product>> getAllProducts() {
        return adminProductRepository.getAllProducts();
    }

    public LiveData<List<Product>> searchProducts(String searchQuery) {
        return adminProductRepository.searchProducts(searchQuery);
    }

    public LiveData<List<Product>> getProductsByCategory(int categoryId) {
        return adminProductRepository.getProductsByCategory(categoryId);
    }

    public LiveData<List<Product>> searchProductsByCategory(String searchQuery, int categoryId) {
        return adminProductRepository.searchProductsByCategory(searchQuery, categoryId);
    }

    public void insertProduct(Product product) {
        executor.execute(() -> adminProductRepository.insertProduct(product));
    }

    public void updateProduct(Product product) {
        executor.execute(() -> adminProductRepository.updateProduct(product));
    }

    public void deleteProduct(Product product) {
        executor.execute(() -> adminProductRepository.deleteProduct(product));
    }

    public MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }
} 