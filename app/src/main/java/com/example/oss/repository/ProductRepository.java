package com.example.oss.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.example.oss.database.AppDatabase;
import com.example.oss.dao.ProductDao;
import com.example.oss.entity.Product;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProductRepository {
    private ProductDao productDao;
    private LiveData<List<Product>> allProducts;
    private ExecutorService executor;

    public ProductRepository(Application application) {
        AppDatabase database = AppDatabase.getDatabase(application);
        productDao = database.productDao();
        allProducts = productDao.getAllActiveProducts();
        executor = Executors.newFixedThreadPool(2);
    }

    public LiveData<List<Product>> getAllProducts() {
        return allProducts;
    }

    public LiveData<List<Product>> getProductsByCategory(int categoryId) {
        return productDao.getProductsByCategory(categoryId);
    }

    public LiveData<Product> getProductById(int id) {
        return productDao.getProductById(id);
    }

    public void insertProduct(Product product) {
        executor.execute(() -> productDao.insertProduct(product));
    }

    public void updateProduct(Product product) {
        executor.execute(() -> productDao.updateProduct(product));
    }

    public void deleteProduct(Product product) {
        executor.execute(() -> productDao.deleteProduct(product));
    }
}