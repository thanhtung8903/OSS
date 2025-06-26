package com.example.oss.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.example.oss.database.AppDatabase;
import com.example.oss.dao.ProductDao;
import com.example.oss.entity.Product;
import com.example.oss.util.SearchFilter;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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

    public LiveData<List<Product>> searchProducts(String query) {
        return productDao.searchProducts(query);
    }

    public LiveData<List<Product>> searchProductsWithCategory(String query, int categoryId) {
        return productDao.searchProductsAdvanced(
                query,
                List.of(categoryId),
                BigDecimal.ZERO,
                new BigDecimal("100000000"),
                false,
                "name_asc");
    }

    public LiveData<List<Product>> searchProductsWithCategories(String query, List<Integer> categoryIds) {
        return productDao.searchProductsAdvanced(
                query,
                categoryIds,
                BigDecimal.ZERO,
                new BigDecimal("100000000"),
                false,
                "name_asc");
    }

    public LiveData<List<Product>> getProductsByCategories(List<Integer> categoryIds) {
        return productDao.searchProductsAdvanced(
                "",
                categoryIds,
                BigDecimal.ZERO,
                new BigDecimal("100000000"),
                false,
                "name_asc");
    }

    public LiveData<List<Product>> searchProductsAdvanced(SearchFilter.FilterState filterState) {
        return productDao.searchProductsAdvanced(
                filterState.getSearchQuery(),
                filterState.getCategoryIdsList().isEmpty() ? null : filterState.getCategoryIdsList(),
                filterState.getPriceRange().getMinPrice(),
                filterState.getPriceRange().getMaxPrice(),
                filterState.isInStockOnly(),
                filterState.getSortOption().getValue());
    }

    public LiveData<BigDecimal> getMinPrice() {
        return productDao.getMinPrice();
    }

    public LiveData<BigDecimal> getMaxPrice() {
        return productDao.getMaxPrice();
    }

    public Future<SearchFilter.PriceRange> getPriceRangeForFilter(String searchQuery, List<Integer> categoryIds) {
        return executor.submit(() -> {
            BigDecimal minPrice = productDao.getMinPriceForFilter(searchQuery, categoryIds);
            BigDecimal maxPrice = productDao.getMaxPriceForFilter(searchQuery, categoryIds);

            if (minPrice == null)
                minPrice = BigDecimal.ZERO;
            if (maxPrice == null)
                maxPrice = new BigDecimal("100000000");

            return new SearchFilter.PriceRange(minPrice, maxPrice);
        });
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