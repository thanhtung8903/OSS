package com.example.oss.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.oss.MainActivity;
import com.example.oss.R;
import com.example.oss.activity.ProductDetailActivity;
import com.example.oss.adapter.CategoryAdapter;
import com.example.oss.adapter.ProductAdapter;
import com.example.oss.entity.Category;
import com.example.oss.entity.Product;
import com.example.oss.viewmodel.ProductViewModel;
import com.example.oss.viewmodel.CartViewModel;
import com.example.oss.viewmodel.WishlistViewModel;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class HomeFragment extends BaseFragment {

    private ProductViewModel productViewModel;
    private RecyclerView rvCategories, rvProducts;
    private TextInputEditText etSearch;
    private TextView tvProductsTitle;

    // Adapters
    private CategoryAdapter categoryAdapter;
    private ProductAdapter productAdapter;

    private CartViewModel cartViewModel;
    private WishlistViewModel wishlistViewModel;

    // Current filter state
    private int currentCategoryFilter = -1; // -1 means show all

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize ViewModels
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        wishlistViewModel = new ViewModelProvider(this).get(WishlistViewModel.class);

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
        tvProductsTitle = view.findViewById(R.id.tv_products_title);

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
        categoryAdapter.setOnShowAllClickListener(this::onShowAllClick);
        rvCategories.setAdapter(categoryAdapter);
    }

    private void setupProductsRecyclerView() {
        // Grid layout for products
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        rvProducts.setLayoutManager(layoutManager);

        // Initialize adapter với wishlist listener
        productAdapter = new ProductAdapter(new ArrayList<>(),
                this::onProductClick,
                this::onAddToCartClick,
                this::onWishlistClick);
        rvProducts.setAdapter(productAdapter);
    }

    private void setupSearch() {
        etSearch.setOnClickListener(v -> {
            // Navigate to SearchFragment
            if (getActivity() instanceof MainActivity) {
                MainActivity mainActivity = (MainActivity) getActivity();
                // Switch to search tab
                mainActivity.findViewById(R.id.nav_search).performClick();
            }
        });

        // Optional: Prevent typing, chỉ cho phép click để navigate
        etSearch.setFocusable(false);
        etSearch.setClickable(true);
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

        // Observe wishlist products để update UI
        if (isLoggedIn()) {
            wishlistViewModel.getWishlistProducts().observe(getViewLifecycleOwner(), wishlistProducts -> {
                if (wishlistProducts != null) {
                    Set<Integer> wishlistProductIds = new HashSet<>();
                    for (Product product : wishlistProducts) {
                        wishlistProductIds.add(product.getId());
                    }
                    productAdapter.updateWishlistProducts(wishlistProductIds);
                }
            });

            // Observe wishlist messages
            wishlistViewModel.getSuccessMessage().observe(getViewLifecycleOwner(), message -> {
                if (message != null && !message.isEmpty()) {
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    wishlistViewModel.clearSuccess();
                }
            });

            wishlistViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
                if (error != null && !error.isEmpty()) {
                    Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                    wishlistViewModel.clearError();
                }
            });
        }
    }

    private void loadData() {
        // Data will be loaded automatically through observers
    }

    private void loadProductsByCategory(int categoryId) {
        currentCategoryFilter = categoryId;
        // Observe products by category
        productViewModel.getProductsByCategory(categoryId).observe(getViewLifecycleOwner(), products -> {
            if (products != null) {
                productAdapter.updateProducts(products);

                String message = products.isEmpty() ? "Không có sản phẩm nào trong danh mục này"
                        : "Hiển thị " + products.size() + " sản phẩm";
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadAllProducts() {
        currentCategoryFilter = -1; // Reset filter
        categoryAdapter.clearSelection(); // Clear category selection

        // Load all products (reset filter)
        productViewModel.getAllProducts().observe(getViewLifecycleOwner(), products -> {
            if (products != null) {
                productAdapter.updateProducts(products);
                Toast.makeText(getContext(), "Hiển thị tất cả sản phẩm", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onCategoryClick(Category category) {
        Toast.makeText(getContext(), "Hiển thị sản phẩm danh mục: " + category.getName(), Toast.LENGTH_SHORT).show();

        // Filter products by category
        loadProductsByCategory(category.getId());

        // Update category selection state
        categoryAdapter.setSelectedCategory(category.getId());

        // Update products title
        updateProductsTitle("Sản phẩm " + category.getName());
    }

    private void onShowAllClick() {
        Toast.makeText(getContext(), "Hiển thị tất cả sản phẩm", Toast.LENGTH_SHORT).show();

        // Load all products
        loadAllProducts();

        // Update products title
        updateProductsTitle("Sản phẩm nổi bật");
    }

    private void updateProductsTitle(String title) {
        if (tvProductsTitle != null) {
            tvProductsTitle.setText(title);
        }
    }

    private void onProductClick(Product product) {
        // Navigate to Product Detail Activity
        Intent intent = new Intent(getActivity(), ProductDetailActivity.class);
        intent.putExtra(ProductDetailActivity.EXTRA_PRODUCT_ID, product.getId());
        startActivity(intent);
    }

    private void onAddToCartClick(Product product) {
        // Check if user is logged in
        if (!isLoggedIn()) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập để thêm vào giỏ hàng", Toast.LENGTH_LONG).show();
            requireLogin();
            return;
        }

        // Initialize CartViewModel if not already done
        if (cartViewModel == null) {
            cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);
        }

        // Add to cart
        cartViewModel.addToCart(product.getId(), 1);
        Toast.makeText(getContext(), "Đã thêm " + product.getName() + " vào giỏ hàng", Toast.LENGTH_SHORT).show();
    }

    private void onWishlistClick(Product product) {
        if (!isLoggedIn()) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập để sử dụng danh sách yêu thích", Toast.LENGTH_LONG).show();
            requireLogin();
            return;
        }

        wishlistViewModel.toggleWishlist(product.getId());
    }
}