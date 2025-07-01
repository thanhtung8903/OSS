package com.example.oss.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.oss.entity.Product;
import com.example.oss.repository.ProductRepository;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdminProductViewModel extends AndroidViewModel {
    private final ProductRepository productRepository;
    private final ExecutorService executor;
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public AdminProductViewModel(@NonNull Application application) {
        super(application);
        productRepository = new ProductRepository(application);
        executor = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Product>> getAllProducts() {
        return productRepository.getAllProducts();
    }

    public void insertProduct(Product product) {
        executor.execute(() -> productRepository.insertProduct(product));
    }

    public void updateProduct(Product product) {
        executor.execute(() -> productRepository.updateProduct(product));
    }

    public void deleteProduct(Product product) {
        executor.execute(() -> productRepository.deleteProduct(product));
    }

    public MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }
} 