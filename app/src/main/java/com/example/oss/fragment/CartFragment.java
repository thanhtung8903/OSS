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
import com.google.android.material.card.MaterialCardView;

public class CartFragment extends BaseFragment {

    private RecyclerView rvCartItems;
    private View layoutEmptyCart;
    private View layoutNotLoggedIn;
    private View layoutCartContent;
    private TextView tvEmptyMessage;
    private TextView tvTotalAmount, tvItemCount;
    private MaterialButton btnLogin, btnBrowseProducts, btnCheckout;
    private MaterialCardView cardSummary;

    // TODO: Add CartAdapter and CartViewModel when created
    // private CartAdapter cartAdapter;
    // private CartViewModel cartViewModel;

    private double totalAmount = 0.0;
    private int totalItems = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cart, container, false);
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
        rvCartItems = view.findViewById(R.id.rv_cart_items);
        layoutEmptyCart = view.findViewById(R.id.layout_empty_cart);
        layoutNotLoggedIn = view.findViewById(R.id.layout_not_logged_in);
        layoutCartContent = view.findViewById(R.id.layout_cart_content);
        tvEmptyMessage = view.findViewById(R.id.tv_empty_message);
        tvTotalAmount = view.findViewById(R.id.tv_total_amount);
        tvItemCount = view.findViewById(R.id.tv_item_count);
        btnLogin = view.findViewById(R.id.btn_login);
        btnBrowseProducts = view.findViewById(R.id.btn_browse_products_not_logged_in);
        btnCheckout = view.findViewById(R.id.btn_checkout);
        cardSummary = view.findViewById(R.id.card_summary);
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
                Toast.makeText(getContext(), "Đang chuyển về trang chủ...", Toast.LENGTH_SHORT).show();
            }
        });

        btnCheckout.setOnClickListener(v -> {
            if (totalItems > 0) {
                performCheckout();
            } else {
                Toast.makeText(getContext(), "Giỏ hàng trống", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        rvCartItems.setLayoutManager(layoutManager);

        // TODO: Initialize and set adapter when CartAdapter is created
        // cartAdapter = new CartAdapter(cartItems, this::onQuantityChanged,
        // this::onRemoveFromCart);
        // rvCartItems.setAdapter(cartAdapter);
    }

    private void checkLoginAndLoadData() {
        if (isLoggedIn()) {
            showLoggedInUI();
            loadCartItems();
        } else {
            showNotLoggedInUI();
        }
    }

    private void loadCartItems() {
        // TODO: Load cart items from ViewModel
        // cartViewModel.getCartItems().observe(this, items -> {
        // if (items.isEmpty()) {
        // showEmptyCart();
        // } else {
        // showCartItems(items);
        // calculateTotal(items);
        // }
        // });

        // Mock for now - show empty cart
        showEmptyCart();
    }

    private void onQuantityChanged(int productId, int newQuantity) {
        // TODO: Update quantity in cart
        // cartViewModel.updateQuantity(productId, newQuantity);
        Toast.makeText(getContext(), "Đã cập nhật số lượng", Toast.LENGTH_SHORT).show();

        // Recalculate total
        // calculateTotal();
    }

    private void onRemoveFromCart(int productId) {
        // TODO: Remove item from cart
        // cartViewModel.removeFromCart(productId);
        Toast.makeText(getContext(), "Đã xóa khỏi giỏ hàng", Toast.LENGTH_SHORT).show();
    }

    private void calculateTotal() {
        // TODO: Calculate total from cart items
        // Mock calculation
        totalAmount = 299000.0; // Sample amount
        totalItems = 2; // Sample item count

        updateSummary();
    }

    private void updateSummary() {
        tvTotalAmount.setText(String.format("%,.0f₫", totalAmount));
        tvItemCount.setText(String.format("%d sản phẩm", totalItems));
    }

    private void performCheckout() {
        // TODO: Navigate to checkout process
        Toast.makeText(getContext(), "Đang chuyển đến thanh toán...", Toast.LENGTH_SHORT).show();
    }

    private void showNotLoggedInUI() {
        hideAllStates();
        layoutNotLoggedIn.setVisibility(View.VISIBLE);
    }

    private void showLoggedInUI() {
        hideAllStates();
        layoutCartContent.setVisibility(View.VISIBLE);
        setupRecyclerView();
    }

    private void showEmptyCart() {
        rvCartItems.setVisibility(View.GONE);
        cardSummary.setVisibility(View.GONE);
        layoutEmptyCart.setVisibility(View.VISIBLE);
        tvEmptyMessage.setText("Giỏ hàng của bạn đang trống");
    }

    private void showCartItems() {
        layoutEmptyCart.setVisibility(View.GONE);
        rvCartItems.setVisibility(View.VISIBLE);
        cardSummary.setVisibility(View.VISIBLE);
        calculateTotal();
    }

    private void hideAllStates() {
        layoutNotLoggedIn.setVisibility(View.GONE);
        layoutCartContent.setVisibility(View.GONE);
    }
}