package com.example.oss.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.oss.R;
import com.example.oss.adapter.AdminProductAdapter;
import com.example.oss.dialog.AdminProductEditDialog;
import com.example.oss.entity.Category;
import com.example.oss.entity.Product;
import com.example.oss.repository.CategoryRepository;
import com.example.oss.viewmodel.AdminProductViewModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class AdminProductManagementFragment extends Fragment {
    private RecyclerView recyclerView;
    private AdminProductAdapter adapter;
    private AdminProductViewModel viewModel;
    private FloatingActionButton fabAddProduct;
    private MaterialToolbar toolbar;
    private TextInputEditText etSearch;
    private ChipGroup categoryChipGroup;
    private CategoryRepository categoryRepository;
    private List<Category> categoryList = new ArrayList<>();
    private String currentSearchQuery = "";
    private int selectedCategoryId = -1; // -1 means all categories

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_product_management, container, false);

        // Initialize views
        toolbar = view.findViewById(R.id.toolbar);
        recyclerView = view.findViewById(R.id.recycler_admin_products);
        fabAddProduct = view.findViewById(R.id.fab_add_product);
        etSearch = view.findViewById(R.id.et_search);
        categoryChipGroup = view.findViewById(R.id.category_chip_group);

        // Setup toolbar
        toolbar.setNavigationOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        // Setup RecyclerView
        adapter = new AdminProductAdapter(new ArrayList<>(), this::onEditProduct, this::onDeleteProduct);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // Setup ViewModel
        viewModel = new ViewModelProvider(this).get(AdminProductViewModel.class);

        // Setup CategoryRepository
        categoryRepository = new CategoryRepository(requireActivity().getApplication());

        // Setup search functionality
        setupSearch();

        // Setup category filter
        setupCategoryFilter();

        // Setup FAB
        fabAddProduct.setOnClickListener(v -> onAddProduct());

        // Load initial data
        loadProducts();

        return view;
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                currentSearchQuery = s.toString().trim();
                loadProducts();
            }
        });
    }

    private void setupCategoryFilter() {
        categoryRepository.getAllCategories().observe(getViewLifecycleOwner(), categories -> {
            categoryList = categories;
            categoryChipGroup.removeAllViews();

            // Add "All Categories" chip
            Chip allChip = new Chip(requireContext());
            allChip.setText("Tất cả");
            allChip.setCheckable(true);
            allChip.setChecked(true);
            allChip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedCategoryId = -1;
                    loadProducts();
                }
            });
            categoryChipGroup.addView(allChip);

            // Add category chips
            for (Category category : categories) {
                Chip chip = new Chip(requireContext());
                chip.setText(category.getName());
                chip.setCheckable(true);
                chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        selectedCategoryId = category.getId();
                        loadProducts();
                    }
                });
                categoryChipGroup.addView(chip);
            }
        });
    }

    private void loadProducts() {
        if (selectedCategoryId == -1) {
            // No category filter
            if (currentSearchQuery.isEmpty()) {
                viewModel.getAllProducts().observe(getViewLifecycleOwner(), products ->
                        adapter.updateProducts(products));
            } else {
                viewModel.searchProducts(currentSearchQuery).observe(getViewLifecycleOwner(), products ->
                        adapter.updateProducts(products));
            }
        } else {
            // With category filter
            if (currentSearchQuery.isEmpty()) {
                viewModel.getProductsByCategory(selectedCategoryId).observe(getViewLifecycleOwner(), products ->
                        adapter.updateProducts(products));
            } else {
                viewModel.searchProductsByCategory(currentSearchQuery, selectedCategoryId).observe(getViewLifecycleOwner(), products ->
                        adapter.updateProducts(products));
            }
        }
    }

    private void onAddProduct() {
        AdminProductEditDialog dialog = AdminProductEditDialog.newInstance(null);
        dialog.setOnProductSavedListener(product -> viewModel.insertProduct(product));
        dialog.show(getParentFragmentManager(), "AddProductDialog");
    }

    private void onEditProduct(int productId) {
        Product product = findProductById(productId);
        if (product != null) {
            AdminProductEditDialog dialog = AdminProductEditDialog.newInstance(product);
            dialog.setOnProductSavedListener(updatedProduct -> viewModel.updateProduct(updatedProduct));
            dialog.show(getParentFragmentManager(), "EditProductDialog");
        }
    }

    private void onDeleteProduct(int productId) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa sản phẩm")
                .setMessage("Bạn có chắc chắn muốn xóa sản phẩm này?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    Product product = findProductById(productId);
                    if (product != null) viewModel.deleteProduct(product);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private Product findProductById(int productId) {
        if (adapter == null) return null;
        for (Product p : adapter.getProducts()) {
            if (p.getId() == productId) return p;
        }
        return null;
    }
} 