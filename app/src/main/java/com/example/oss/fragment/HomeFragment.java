package com.example.oss.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.oss.R;
import com.example.oss.adapter.CategoryAdapter;
import com.example.oss.adapter.ProductAdapter;
import com.example.oss.entity.Category;
import com.example.oss.entity.Product;
import com.example.oss.viewmodel.ProductViewModel;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;

public class HomeFragment extends BaseFragment {

    private ProductViewModel productViewModel;
    private RecyclerView rvCategories, rvProducts;
    private TextInputEditText etSearch;

    // Adapters
    private CategoryAdapter categoryAdapter;
    private ProductAdapter productAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize ViewModel
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);

        // Setup UI
        setupViews(view);
        setupObservers();

        // Load data
        loadData();
    }

    private void setupViews(View view) {
        etSearch = view.findViewById(R.id.et_search);
        rvCategories = view.findViewById(R.id.rv_categories);
        rvProducts = view.findViewById(R.id.rv_products);

        // Setup RecyclerViews
        setupCategoriesRecyclerView();
        setupProductsRecyclerView();

        // Setup search
        setupSearch();
    }

    private void setupCategoriesRecyclerView() {
        // Horizontal scroll for categories
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL,
                false);
        rvCategories.setLayoutManager(layoutManager);

        // Initialize adapter
        categoryAdapter = new CategoryAdapter(new ArrayList<>(), this::onCategoryClick);
        rvCategories.setAdapter(categoryAdapter);
    }

    private void setupProductsRecyclerView() {
        // Grid layout for products
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        rvProducts.setLayoutManager(layoutManager);

        // Initialize adapter
        productAdapter = new ProductAdapter(new ArrayList<>(), this::onProductClick, this::onAddToCartClick);
        rvProducts.setAdapter(productAdapter);
    }

    private void setupSearch() {
        etSearch.setOnClickListener(v -> {
            // Navigate to SearchFragment
            Toast.makeText(getContext(), "Chuyển đến trang tìm kiếm...", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupObservers() {
        // Observe categories
        productViewModel.getAllCategories().observe(getViewLifecycleOwner(), categories -> {
            if (categories != null) {
                categoryAdapter.updateCategories(categories);
                if (!categories.isEmpty()) {
                    Toast.makeText(getContext(), "Đã tải " + categories.size() + " danh mục", Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });

        // Observe products
        productViewModel.getAllProducts().observe(getViewLifecycleOwner(), products -> {
            if (products != null) {
                productAdapter.updateProducts(products);
                if (!products.isEmpty()) {
                    Toast.makeText(getContext(), "Đã tải " + products.size() + " sản phẩm", Toast.LENGTH_SHORT).show();
                } else {
                    // Insert sample data if no products
                    productViewModel.insertSampleData();
                }
            }
        });

        // Observe loading state
        productViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            // TODO: Show/hide loading indicator
        });

        // Observe errors
        productViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                productViewModel.clearError();
            }
        });
    }

    private void loadData() {
        // Data will be loaded automatically through observers
    }

    private void onCategoryClick(Category category) {
        Toast.makeText(getContext(), "Danh mục: " + category.getName(), Toast.LENGTH_SHORT).show();
        // TODO: Filter products by category
        // productViewModel.getProductsByCategory(category.getId()).observe(this,
        // products -> {
        // productAdapter.updateProducts(products);
        // });
    }

    private void onProductClick(Product product) {
        Toast.makeText(getContext(), "Sản phẩm: " + product.getName(), Toast.LENGTH_SHORT).show();
        // TODO: Navigate to product detail
    }

    private void onAddToCartClick(Product product) {
        // Check if user is logged in
        if (!isLoggedIn()) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập để thêm vào giỏ hàng", Toast.LENGTH_LONG).show();
            requireLogin();
            return;
        }

        Toast.makeText(getContext(), "Đã thêm " + product.getName() + " vào giỏ hàng", Toast.LENGTH_SHORT).show();
        // TODO: Add to cart logic
        // cartViewModel.addToCart(product.getId(), 1);
    }
}