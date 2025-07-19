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
        try {
            List<Integer> categoryIds = filterState.getCategoryIdsList();
            android.util.Log.d("ProductRepository", "Search params - Query: '" +
                    filterState.getSearchQuery() + "', Categories: " +
                    (categoryIds.isEmpty() ? "null" : categoryIds.toString()) +
                    ", Price: " + filterState.getPriceRange().getMinPrice() +
                    " - " + filterState.getPriceRange().getMaxPrice() +
                    ", InStock: " + filterState.isInStockOnly() +
                    ", Sort: " + filterState.getSortOption().getValue());

            // Debug: Test simple search first
            if (!filterState.getSearchQuery().isEmpty()) {
                android.util.Log.d("ProductRepository", "Testing simple search for: " + filterState.getSearchQuery());
                LiveData<List<Product>> simpleResult = productDao.searchProducts(filterState.getSearchQuery());
                // Log this result in observer
            }

            // Try workaround for BigDecimal issues
            BigDecimal minPrice = filterState.getPriceRange().getMinPrice();
            BigDecimal maxPrice = filterState.getPriceRange().getMaxPrice();

            // Debug price values
            android.util.Log.d("ProductRepository", "Price values - Min: " + minPrice +
                    " (type: " + minPrice.getClass().getSimpleName() + "), Max: " + maxPrice +
                    " (type: " + maxPrice.getClass().getSimpleName() + ")");

            return productDao.searchProductsAdvanced(
                    filterState.getSearchQuery(),
                    categoryIds.isEmpty() ? null : categoryIds,
                    minPrice,
                    maxPrice,
                    filterState.isInStockOnly(),
                    filterState.getSortOption().getValue());
        } catch (Exception e) {
            android.util.Log.e("ProductRepository", "Error in searchProductsAdvanced", e);
            // Return empty LiveData instead of crashing
            return new androidx.lifecycle.MutableLiveData<>(new java.util.ArrayList<>());
        }
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

    // Debug methods
    public void debugDatabaseState(String searchQuery) {
        executor.execute(() -> {
            try {
                int totalProducts = productDao.getTotalProductCount();
                int activeProducts = productDao.getActiveProductCount();
                android.util.Log.d("ProductRepository",
                        "Database state - Total: " + totalProducts + ", Active: " + activeProducts);

                if (!searchQuery.isEmpty()) {
                    List<Product> debugResults = productDao.debugSearchSync(searchQuery);
                    android.util.Log.d("ProductRepository",
                            "Debug search for '" + searchQuery + "' returned " + debugResults.size() + " products:");
                    for (Product p : debugResults) {
                        android.util.Log.d("ProductRepository",
                                "  - " + p.getName() + " (ID: " + p.getId() + ", Active: " + p.isActive() + ")");
                    }

                    // Test advanced search query without price constraints
                    List<Product> advancedDebugResults = productDao.debugAdvancedSearchSimpleSync(searchQuery);
                    android.util.Log.d("ProductRepository", "Advanced debug search for '" + searchQuery + "' returned "
                            + advancedDebugResults.size() + " products:");
                    for (Product p : advancedDebugResults) {
                        android.util.Log.d("ProductRepository",
                                "  ADV - " + p.getName() + " (ID: " + p.getId() + ", Price: " + p.getPrice() + ")");
                    }

                    // Test advanced search WITH hardcoded price range
                    List<Product> advancedWithPriceResults = productDao.debugAdvancedSearchWithPriceSync(searchQuery);
                    android.util.Log.d("ProductRepository", "Advanced+Price debug search for '" + searchQuery
                            + "' returned " + advancedWithPriceResults.size() + " products:");
                    for (Product p : advancedWithPriceResults) {
                        android.util.Log.d("ProductRepository",
                                "  PRICE - " + p.getName() + " (ID: " + p.getId() + ", Price: " + p.getPrice() + ")");
                    }
                }

                // Test with all products
                List<Product> allProducts = productDao.getAllActiveProductsSync();
                android.util.Log.d("ProductRepository", "All active products (" + allProducts.size() + "):");
                for (Product p : allProducts) {
                    if (p.getName().toLowerCase().contains(searchQuery.toLowerCase())) {
                        android.util.Log.d("ProductRepository", "  MATCH: " + p.getName() + " (ID: " + p.getId() +
                                ", Price: " + p.getPrice() + ", Stock: " + p.getStockQuantity() + ")");
                    }
                }
            } catch (Exception e) {
                android.util.Log.e("ProductRepository", "Error in debugDatabaseState", e);
            }
        });
    }
}