package com.example.oss.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.oss.MainActivity;
import com.example.oss.R;
import com.google.android.material.button.MaterialButton;

public class WishlistFragment extends BaseFragment {

    private RecyclerView rvWishlist;
    private View layoutEmptyState;
    private View layoutNotLoggedIn;
    private TextView tvEmptyMessage;
    private MaterialButton btnLogin, btnBrowseProducts;

    // TODO: Add WishlistAdapter and WishlistViewModel when created
    // private WishlistAdapter wishlistAdapter;
    // private WishlistViewModel wishlistViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wishlist, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupListeners();

        // Check login status and show appropriate UI
        checkLoginAndLoadData();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Re-check login status when returning to fragment
        checkLoginAndLoadData();
    }

    private void initViews(View view) {
        rvWishlist = view.findViewById(R.id.rv_wishlist);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);
        layoutNotLoggedIn = view.findViewById(R.id.layout_not_logged_in);
        tvEmptyMessage = view.findViewById(R.id.tv_empty_message);
        btnLogin = view.findViewById(R.id.btn_login);
        btnBrowseProducts = view.findViewById(R.id.btn_browse_products_not_logged_in);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> {
            if (!requireLogin()) {
                // User will be redirected to login
                return;
            }
        });

        btnBrowseProducts.setOnClickListener(v -> {
            // Navigate to Home fragment
            if (getActivity() instanceof MainActivity) {
                // TODO: Navigate to home fragment
                Toast.makeText(getContext(), "Navigating to browse products...", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        rvWishlist.setLayoutManager(layoutManager);

        // TODO: Initialize and set adapter when WishlistAdapter is created
        // wishlistAdapter = new WishlistAdapter(wishlistItems,
        // this::onWishlistItemClick, this::onRemoveFromWishlist);
        // rvWishlist.setAdapter(wishlistAdapter);
    }

    private void checkLoginAndLoadData() {
        if (isLoggedIn()) {
            showLoggedInUI();
            loadWishlistItems();
        } else {
            showNotLoggedInUI();
        }
    }

    private void loadWishlistItems() {
        // TODO: Load wishlist items from ViewModel
        // wishlistViewModel.getWishlistItems().observe(this, items -> {
        // if (items.isEmpty()) {
        // showEmptyWishlist();
        // } else {
        // showWishlistItems(items);
        // }
        // });

        // Mock for now - show empty state
        showEmptyWishlist();
    }

    private void onWishlistItemClick(int productId) {
        // TODO: Navigate to product detail
        Toast.makeText(getContext(), "Product clicked: " + productId, Toast.LENGTH_SHORT).show();
    }

    private void onRemoveFromWishlist(int productId) {
        // TODO: Remove item from wishlist
        // wishlistViewModel.removeFromWishlist(productId);
        Toast.makeText(getContext(), "Đã xóa khỏi danh sách yêu thích", Toast.LENGTH_SHORT).show();
    }

    private void onAddToCart(int productId) {
        // TODO: Add to cart
        Toast.makeText(getContext(), "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
    }

    private void showNotLoggedInUI() {
        hideAllStates();
        layoutNotLoggedIn.setVisibility(View.VISIBLE);
    }

    private void showLoggedInUI() {
        hideAllStates();
        setupRecyclerView();
    }

    private void showEmptyWishlist() {
        hideAllStates();
        layoutEmptyState.setVisibility(View.VISIBLE);
        tvEmptyMessage.setText("Danh sách yêu thích của bạn đang trống");
    }

    private void showWishlistItems() {
        hideAllStates();
        rvWishlist.setVisibility(View.VISIBLE);
    }

    private void hideAllStates() {
        layoutNotLoggedIn.setVisibility(View.GONE);
        layoutEmptyState.setVisibility(View.GONE);
        rvWishlist.setVisibility(View.GONE);
    }
}