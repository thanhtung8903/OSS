package com.example.oss.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.oss.R;
import com.example.oss.activity.ProductDetailActivity;
import com.example.oss.adapter.CategoryFilterAdapter;
import com.example.oss.adapter.ProductAdapter;
import com.example.oss.dialog.PriceFilterDialog;
import com.example.oss.dialog.SortOptionsDialog;
import com.example.oss.entity.Category;
import com.example.oss.entity.Product;
import com.example.oss.util.SearchFilter;
import com.example.oss.viewmodel.CartViewModel;
import com.example.oss.viewmodel.ProductViewModel;
import com.example.oss.viewmodel.WishlistViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SearchFragment extends BaseFragment implements
        ProductAdapter.OnProductClickListener,
        ProductAdapter.OnAddToCartClickListener,
        ProductAdapter.OnWishlistClickListener,
        CategoryFilterAdapter.OnCategoryFilterChangeListener,
        PriceFilterDialog.OnPriceFilterListener,
        SortOptionsDialog.OnSortOptionListener {

    // UI Components
    private TextInputEditText etSearch;
    private RecyclerView rvSearchResults;
    private View layoutEmptyState;
    private View layoutSearchPrompt;
    private View layoutLoading;

    // Filter Section
    private View layoutFilterSection;
    private RecyclerView rvCategoryFilters;
    private MaterialButton btnToggleFilters;
    private MaterialButton btnPriceFilter;
    private MaterialButton btnSortOptions;
    private MaterialButton btnClearFilters;
    private CheckBox cbInStockOnly;
    private TextView tvSearchResultsCount;
    private ChipGroup chipGroupActiveFilters;

    // ViewModels
    private ProductAdapter productAdapter;
    private ProductViewModel productViewModel;
    private WishlistViewModel wishlistViewModel;
    private CartViewModel cartViewModel;

    // Adapters
    private CategoryFilterAdapter categoryFilterAdapter;

    // Filter State Management
    private SearchFilter.FilterState currentFilter = new SearchFilter.FilterState();
    private List<Category> allCategories = new ArrayList<>();
    private SearchFilter.PriceRange availablePriceRange = new SearchFilter.PriceRange();
    private boolean wishlistObserversSetup = false;
    private boolean isFilterSectionVisible = false;

    // Search state
    private final int SEARCH_DELAY_MS = 500;
    private Runnable searchRunnable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViewModels();
        initViews(view);
        setupListeners();
        setupRecyclerView();
        setupObservers();

        // Load initial data
        loadAvailablePriceRange();

        // Show initial search prompt
        showSearchPrompt();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Setup wishlist observers when user might have logged in
        if (isLoggedIn() && !wishlistObserversSetup) {
            setupWishlistObservers();
        }
    }

    private void initViewModels() {
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        wishlistViewModel = new ViewModelProvider(this).get(WishlistViewModel.class);
        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);
    }

    private void initViews(View view) {
        // Search views
        etSearch = view.findViewById(R.id.et_search);
        rvSearchResults = view.findViewById(R.id.rv_search_results);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);
        layoutSearchPrompt = view.findViewById(R.id.layout_search_prompt);
        layoutLoading = view.findViewById(R.id.layout_loading);

        // Filter views
        layoutFilterSection = view.findViewById(R.id.layout_filter_section);
        rvCategoryFilters = view.findViewById(R.id.rv_category_filters);
        btnToggleFilters = view.findViewById(R.id.btn_toggle_filters);
        btnPriceFilter = view.findViewById(R.id.btn_price_filter);
        btnSortOptions = view.findViewById(R.id.btn_sort_options);
        btnClearFilters = view.findViewById(R.id.btn_clear_filters);
        cbInStockOnly = view.findViewById(R.id.cb_in_stock_only);
        tvSearchResultsCount = view.findViewById(R.id.tv_search_results_count);
        chipGroupActiveFilters = view.findViewById(R.id.chip_group_active_filters);
    }

    private void setupListeners() {
        // Search listeners
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();
                currentFilter.setSearchQuery(query);

                // Cancel previous search
                if (searchRunnable != null) {
                    etSearch.removeCallbacks(searchRunnable);
                }

                if (query.length() >= 2 || hasActiveFilters()) {
                    showLoadingState();
                    searchRunnable = () -> performSearch();
                    etSearch.postDelayed(searchRunnable, SEARCH_DELAY_MS);
                } else if (query.isEmpty() && !hasActiveFilters()) {
                    showSearchPrompt();
                }
            }
        });

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            performSearch();
            return true;
        });

        // Filter button listeners
        btnToggleFilters.setOnClickListener(v -> toggleFilterSection());
        btnPriceFilter.setOnClickListener(v -> showPriceFilterDialog());
        btnSortOptions.setOnClickListener(v -> showSortOptionsDialog());
        btnClearFilters.setOnClickListener(v -> clearAllFilters());

        // Stock filter listener
        cbInStockOnly.setOnCheckedChangeListener((buttonView, isChecked) -> {
            currentFilter.setInStockOnly(isChecked);
            updateActiveFiltersDisplay();
            performSearch();
        });
    }

    private void setupRecyclerView() {
        // Setup products RecyclerView
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        rvSearchResults.setLayoutManager(layoutManager);
        productAdapter = new ProductAdapter(new ArrayList<>(), this, this, this);
        rvSearchResults.setAdapter(productAdapter);

        // Setup category filter RecyclerView
        setupCategoryFilterRecyclerView();
    }

    private void setupCategoryFilterRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(),
                LinearLayoutManager.HORIZONTAL, false);
        rvCategoryFilters.setLayoutManager(layoutManager);

        categoryFilterAdapter = new CategoryFilterAdapter(allCategories, this, true);
        rvCategoryFilters.setAdapter(categoryFilterAdapter);
    }

    private void setupObservers() {
        // Observe loading state
        productViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null && isLoading && hasSearchQueryOrFilters()) {
                showLoadingState();
            }
        });

        // Observe error messages
        productViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                productViewModel.clearError();
            }
        });

        // Observe categories
        productViewModel.getAllCategories().observe(getViewLifecycleOwner(), categories -> {
            if (categories != null) {
                allCategories.clear();
                allCategories.addAll(categories);
                categoryFilterAdapter.updateCategories(categories);
            }
        });

        // Setup wishlist observers nếu user đã login
        if (isLoggedIn()) {
            setupWishlistObservers();
        }

        // Setup cart observers
        setupCartObservers();
    }

    private void setupWishlistObservers() {
        if (wishlistObserversSetup) {
            return;
        }

        wishlistViewModel.getWishlistProducts().observe(getViewLifecycleOwner(), wishlistProducts -> {
            if (wishlistProducts != null) {
                Set<Integer> wishlistProductIds = new HashSet<>();
                for (Product product : wishlistProducts) {
                    wishlistProductIds.add(product.getId());
                }
                productAdapter.updateWishlistProducts(wishlistProductIds);
            }
        });

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

        wishlistObserversSetup = true;
    }

    private void setupCartObservers() {
        cartViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                cartViewModel.clearError();
            }
        });
    }

    private void loadAvailablePriceRange() {
        productViewModel.getMinPrice().observe(getViewLifecycleOwner(), minPrice -> {
            if (minPrice != null) {
                availablePriceRange.setMinPrice(minPrice);
            }
        });

        productViewModel.getMaxPrice().observe(getViewLifecycleOwner(), maxPrice -> {
            if (maxPrice != null) {
                availablePriceRange.setMaxPrice(maxPrice);
            }
        });
    }

    private void performSearch() {
        showLoadingState();

        productViewModel.searchProductsAdvanced(currentFilter)
                .observe(getViewLifecycleOwner(), products -> {
                    if (products != null) {
                        updateSearchResults(products);
                    } else {
                        showEmptyState();
                    }
                });
    }

    private void updateSearchResults(List<Product> products) {
        if (products.isEmpty()) {
            showEmptyState();
        } else {
            showResults();
            productAdapter.updateProducts(products);
            updateResultsCount(products.size());

            // Show filter section if we have results
            layoutFilterSection.setVisibility(View.VISIBLE);
        }
    }

    private void updateResultsCount(int count) {
        if (tvSearchResultsCount != null) {
            String countText;
            String query = currentFilter.getSearchQuery();
            if (query.isEmpty()) {
                countText = count + " sản phẩm";
            } else {
                countText = "Tìm thấy " + count + " sản phẩm cho \"" + query + "\"";
            }
            tvSearchResultsCount.setText(countText);
        }
    }

    // CategoryFilterAdapter.OnCategoryFilterChangeListener
    @Override
    public void onCategoryFilterChanged(Set<Integer> selectedCategoryIds) {
        currentFilter.setCategoryIds(selectedCategoryIds);
        updateActiveFiltersDisplay();
        updateClearFiltersButtonVisibility();
        performSearch();
    }

    // PriceFilterDialog.OnPriceFilterListener
    @Override
    public void onPriceFilterApplied(SearchFilter.PriceRange priceRange) {
        currentFilter.setPriceRange(priceRange);
        updateActiveFiltersDisplay();
        updatePriceFilterButtonText();
        performSearch();
    }

    // SortOptionsDialog.OnSortOptionListener
    @Override
    public void onSortOptionSelected(SearchFilter.SortOption sortOption) {
        currentFilter.setSortOption(sortOption);
        updateActiveFiltersDisplay();
        updateSortButtonText();
        performSearch();
    }

    // ProductAdapter Listeners
    @Override
    public void onProductClick(Product product) {
        Intent intent = new Intent(getActivity(), ProductDetailActivity.class);
        intent.putExtra(ProductDetailActivity.EXTRA_PRODUCT_ID, product.getId());
        startActivity(intent);
    }

    @Override
    public void onAddToCartClick(Product product) {
        if (!isLoggedIn()) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập để thêm vào giỏ hàng", Toast.LENGTH_LONG).show();
            requireLogin();
            return;
        }

        if (product.getStockQuantity() <= 0) {
            Toast.makeText(getContext(), "Sản phẩm này hiện đã hết hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        cartViewModel.addToCart(product.getId(), 1);
        Toast.makeText(getContext(), "Đã thêm \"" + product.getName() + "\" vào giỏ hàng", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onWishlistClick(Product product) {
        if (!isLoggedIn()) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập để sử dụng danh sách yêu thích", Toast.LENGTH_LONG).show();
            requireLogin();
            return;
        }

        wishlistViewModel.toggleWishlist(product.getId());
    }

    // Filter Dialog Methods
    private void showPriceFilterDialog() {
        PriceFilterDialog dialog = PriceFilterDialog.newInstance(
                currentFilter.getPriceRange(),
                availablePriceRange);
        dialog.setOnPriceFilterListener(this);
        dialog.show(getChildFragmentManager(), "PriceFilterDialog");
    }

    private void showSortOptionsDialog() {
        SortOptionsDialog dialog = SortOptionsDialog.newInstance(currentFilter.getSortOption());
        dialog.setOnSortOptionListener(this);
        dialog.show(getChildFragmentManager(), "SortOptionsDialog");
    }

    // UI Update Methods
    private void toggleFilterSection() {
        isFilterSectionVisible = !isFilterSectionVisible;

        if (isFilterSectionVisible) {
            rvCategoryFilters.setVisibility(View.VISIBLE);
            btnToggleFilters.setText("Ẩn bộ lọc");
            btnToggleFilters.setIconResource(R.drawable.ic_arrow_back);
        } else {
            rvCategoryFilters.setVisibility(View.GONE);
            btnToggleFilters.setText("Danh mục");
            btnToggleFilters.setIconResource(R.drawable.ic_category_default);
        }
    }

    private void updateActiveFiltersDisplay() {
        if (chipGroupActiveFilters == null)
            return;

        chipGroupActiveFilters.removeAllViews();

        // Add category filter chips
        for (Integer categoryId : currentFilter.getCategoryIds()) {
            Category category = findCategoryById(categoryId);
            if (category != null) {
                addFilterChip(category.getName(), () -> {
                    Set<Integer> newCategoryIds = new HashSet<>(currentFilter.getCategoryIds());
                    newCategoryIds.remove(categoryId);
                    currentFilter.setCategoryIds(newCategoryIds);
                    categoryFilterAdapter.setSelectedCategories(newCategoryIds);
                    updateActiveFiltersDisplay();
                    performSearch();
                });
            }
        }

        // Add price filter chip
        if (!currentFilter.getPriceRange().isDefault()) {
            addFilterChip("Giá: " + currentFilter.getPriceRange().toString(), () -> {
                currentFilter.setPriceRange(new SearchFilter.PriceRange());
                updatePriceFilterButtonText();
                updateActiveFiltersDisplay();
                performSearch();
            });
        }

        // Add sort chip (if not default)
        if (currentFilter.getSortOption() != SearchFilter.SortOption.NAME_ASC) {
            addFilterChip("Sắp xếp: " + currentFilter.getSortOption().getDisplayName(), () -> {
                currentFilter.setSortOption(SearchFilter.SortOption.NAME_ASC);
                updateSortButtonText();
                updateActiveFiltersDisplay();
                performSearch();
            });
        }

        // Add stock filter chip
        if (currentFilter.isInStockOnly()) {
            addFilterChip("Còn hàng", () -> {
                currentFilter.setInStockOnly(false);
                cbInStockOnly.setChecked(false);
                updateActiveFiltersDisplay();
                performSearch();
            });
        }

        updateClearFiltersButtonVisibility();
    }

    private void addFilterChip(String text, Runnable onRemove) {
        Chip chip = new Chip(getContext());
        chip.setText(text);
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(v -> onRemove.run());
        chipGroupActiveFilters.addView(chip);
    }

    private void updatePriceFilterButtonText() {
        if (currentFilter.getPriceRange().isDefault()) {
            btnPriceFilter.setText("Giá");
        } else {
            btnPriceFilter.setText("Giá: " + currentFilter.getPriceRange().toString());
        }
    }

    private void updateSortButtonText() {
        btnSortOptions.setText("Sắp xếp: " + currentFilter.getSortOption().getDisplayName());
    }

    private void clearAllFilters() {
        currentFilter.clearAllFilters();
        categoryFilterAdapter.clearSelection();
        cbInStockOnly.setChecked(false);
        updateActiveFiltersDisplay();
        updatePriceFilterButtonText();
        updateSortButtonText();

        if (!currentFilter.getSearchQuery().isEmpty()) {
            performSearch();
        } else {
            showSearchPrompt();
        }
    }

    private void updateClearFiltersButtonVisibility() {
        btnClearFilters.setVisibility(currentFilter.hasActiveFilters() ? View.VISIBLE : View.GONE);
    }

    // Helper Methods
    private Category findCategoryById(int categoryId) {
        for (Category category : allCategories) {
            if (category.getId() == categoryId) {
                return category;
            }
        }
        return null;
    }

    private boolean hasActiveFilters() {
        return currentFilter.hasActiveFilters();
    }

    private boolean hasSearchQueryOrFilters() {
        return !currentFilter.getSearchQuery().isEmpty() || hasActiveFilters();
    }

    // UI State Methods
    private void showSearchPrompt() {
        hideAllStates();
        layoutSearchPrompt.setVisibility(View.VISIBLE);
        layoutFilterSection.setVisibility(View.GONE);
    }

    private void showLoadingState() {
        hideAllStates();
        if (layoutLoading != null) {
            layoutLoading.setVisibility(View.VISIBLE);
        }
    }

    private void showEmptyState() {
        hideAllStates();
        layoutEmptyState.setVisibility(View.VISIBLE);

        if (layoutEmptyState.findViewById(R.id.tv_empty_title) != null) {
            String query = currentFilter.getSearchQuery();
            String message = query.isEmpty() ? "Không tìm thấy sản phẩm" : "Không tìm thấy \"" + query + "\"";
            ((TextView) layoutEmptyState.findViewById(R.id.tv_empty_title)).setText(message);
        }
    }

    private void showResults() {
        hideAllStates();
        rvSearchResults.setVisibility(View.VISIBLE);
    }

    private void hideAllStates() {
        layoutSearchPrompt.setVisibility(View.GONE);
        layoutEmptyState.setVisibility(View.GONE);
        rvSearchResults.setVisibility(View.GONE);
        if (layoutLoading != null) {
            layoutLoading.setVisibility(View.GONE);
        }
    }

    public void performSearch(String query) {
        if (etSearch != null) {
            etSearch.setText(query);
            etSearch.setSelection(query.length());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (searchRunnable != null && etSearch != null) {
            etSearch.removeCallbacks(searchRunnable);
        }
    }
}