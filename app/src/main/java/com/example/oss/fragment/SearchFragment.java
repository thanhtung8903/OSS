package com.example.oss.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.oss.R;
import com.google.android.material.textfield.TextInputEditText;

public class SearchFragment extends BaseFragment {

    private TextInputEditText etSearch;
    private RecyclerView rvSearchResults;
    private View layoutEmptyState;
    private View layoutSearchPrompt;

    // TODO: Add ProductAdapter when created
    // private ProductAdapter productAdapter;
    // private ProductViewModel productViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupListeners();
        setupRecyclerView();

        // Show initial search prompt
        showSearchPrompt();
    }

    private void initViews(View view) {
        etSearch = view.findViewById(R.id.et_search);
        rvSearchResults = view.findViewById(R.id.rv_search_results);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);
        layoutSearchPrompt = view.findViewById(R.id.layout_search_prompt);
    }

    private void setupListeners() {
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
                if (query.length() >= 2) {
                    searchProducts(query);
                } else if (query.isEmpty()) {
                    showSearchPrompt();
                }
            }
        });
    }

    private void setupRecyclerView() {
        // Setup grid layout for search results
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        rvSearchResults.setLayoutManager(layoutManager);

        // TODO: Initialize and set adapter when ProductAdapter is created
        // productAdapter = new ProductAdapter(products, this::onProductClick);
        // rvSearchResults.setAdapter(productAdapter);
    }

    private void searchProducts(String query) {
        hideAllStates();

        // TODO: Implement search logic with ProductViewModel
        // productViewModel.searchProducts(query);

        // Mock search for now
        // Simulate search delay
        etSearch.postDelayed(() -> {
            // Mock: show empty state or results
            showEmptyState();
        }, 500);
    }

    private void onProductClick(int productId) {
        // TODO: Navigate to product detail
        Toast.makeText(getContext(), "Product clicked: " + productId, Toast.LENGTH_SHORT).show();
    }

    private void showSearchPrompt() {
        hideAllStates();
        layoutSearchPrompt.setVisibility(View.VISIBLE);
    }

    private void showEmptyState() {
        hideAllStates();
        layoutEmptyState.setVisibility(View.VISIBLE);
    }

    private void showResults() {
        hideAllStates();
        rvSearchResults.setVisibility(View.VISIBLE);
    }

    private void hideAllStates() {
        layoutSearchPrompt.setVisibility(View.GONE);
        layoutEmptyState.setVisibility(View.GONE);
        rvSearchResults.setVisibility(View.GONE);
    }
}