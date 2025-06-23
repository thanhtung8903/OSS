package com.example.oss.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.oss.MainActivity;
import com.example.oss.R;
import com.example.oss.adapter.CartAdapter;
import com.example.oss.dao.CartDao;
import com.example.oss.viewmodel.CartViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

import com.example.oss.adapter.CartAdapter;
import com.example.oss.dao.CartDao;
import com.example.oss.viewmodel.CartViewModel;

import java.util.ArrayList;

public class CartFragment extends BaseFragment {

    private RecyclerView rvCartItems;
    private View layoutEmptyCart;
    private View layoutNotLoggedIn;
    private View layoutCartContent;
    private TextView tvEmptyMessage;
    private TextView tvTotalAmount, tvItemCount;
    private MaterialButton btnLogin, btnBrowseProducts, btnCheckout;
    private MaterialCardView cardSummary;

    private double totalAmount = 0.0;
    private int totalItems = 0;

    private CartAdapter cartAdapter;
    private CartViewModel cartViewModel;

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

        // Initialize ViewModels
        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);
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

        cartAdapter = new CartAdapter(new ArrayList<>(), this::onQuantityChanged, this::onRemoveFromCart);
        rvCartItems.setAdapter(cartAdapter);
    }

    private void setupObservers() {
        // Observe cart items
        cartViewModel.getCartItems().observe(getViewLifecycleOwner(), cartItems -> {
            if (cartItems != null) {
                if (cartItems.isEmpty()) {
                    showEmptyCart();
                } else {
                    showCartItems();
                    cartAdapter.updateCartItems(cartItems);
                }
            }
        });

        // Observe cart total
        cartViewModel.getCartTotal().observe(getViewLifecycleOwner(), total -> {
            if (total != null) {
                totalAmount = total.doubleValue();
                updateSummary();
            }
        });

        // Observe cart count
        cartViewModel.getTotalQuantity().observe(getViewLifecycleOwner(), quantity -> {
            if (quantity != null) {
                totalItems = quantity;
                updateSummary();
            }
        });

        // Observe errors
        cartViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                cartViewModel.clearError();
            }
        });
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
        setupObservers();
    }

    private void onQuantityChanged(int productId, int newQuantity) {
        if (cartViewModel != null) {
            cartViewModel.updateQuantity(productId, newQuantity);
            // Trigger refresh total after a short delay để đảm bảo data đã update
            rvCartItems.postDelayed(this::refreshTotal, 100);
        } else {
            Toast.makeText(getContext(), "Lỗi: không thể cập nhật số lượng", Toast.LENGTH_SHORT).show();
        }
    }

    private void onRemoveFromCart(int productId) {
        if (cartViewModel != null) {
            cartViewModel.removeFromCart(productId);
            Toast.makeText(getContext(), "Đã xóa khỏi giỏ hàng", Toast.LENGTH_SHORT).show();
            // Trigger refresh total after a short delay để đảm bảo data đã update
            rvCartItems.postDelayed(this::refreshTotal, 100);
        } else {
            Toast.makeText(getContext(), "Lỗi: không thể xóa sản phẩm", Toast.LENGTH_SHORT).show();
        }
    }

    private void calculateTotal() {
        // Nếu đã có CartViewModel, sử dụng LiveData observers thay vì tính toán manual
        // Method này chỉ cần trigger update hoặc tính toán backup nếu cần
        if (cartViewModel != null && getCurrentUser() != null) {
            // Trigger manual calculation nếu LiveData chưa cập nhật
            cartViewModel.getCartTotal().observe(getViewLifecycleOwner(), total -> {
                if (total != null) {
                    totalAmount = total.doubleValue();
                    updateSummary();
                }
            });

            cartViewModel.getTotalQuantity().observe(getViewLifecycleOwner(), quantity -> {
                if (quantity != null) {
                    totalItems = quantity;
                    updateSummary();
                }
            });
        } else {
            // Fallback calculation từ adapter data nếu cần
            calculateTotalFromAdapter();
        }
    }

    /**
     * Backup method để tính total từ adapter data
     * Sử dụng khi CartViewModel chưa sẵn sàng
     */
    private void calculateTotalFromAdapter() {
        if (cartAdapter == null) {
            totalAmount = 0.0;
            totalItems = 0;
            updateSummary();
            return;
        }

        try {
            // Tính tổng từ adapter data
            totalAmount = cartAdapter.getTotalAmount().doubleValue();
            totalItems = cartAdapter.getTotalQuantity();


        } catch (Exception e) {
            // Fallback nếu có lỗi
            totalAmount = 0.0;
            totalItems = 0;
            android.util.Log.e("CartFragment", "Error calculating total from adapter", e);
        }

        updateSummary();
    }

    /**
     * Method để force refresh total calculation
     * Gọi khi có thay đổi quantity hoặc remove items
     */
    private void refreshTotal() {
        if (cartViewModel != null && getCurrentUser() != null) {
            // Trigger re-observe để cập nhật latest data
            calculateTotal();
        }
    }

    private void updateSummary() {
        // Format currency theo chuẩn Việt Nam
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        String formattedAmount = currencyFormat.format(totalAmount);
        // Thay thế VND với ₫ để hiển thị đẹp hơn
        formattedAmount = formattedAmount.replace("₫", "").trim() + "₫";

        // Cập nhật UI
        if (tvTotalAmount != null) {
            tvTotalAmount.setText(formattedAmount);
        }

        if (tvItemCount != null) {
            // Hiển thị số lượng với unit phù hợp
            String itemText;
            if (totalItems == 0) {
                itemText = "Không có sản phẩm";
            } else if (totalItems == 1) {
                itemText = "1 sản phẩm";
            } else {
                itemText = String.format("%d sản phẩm", totalItems);
            }
            tvItemCount.setText(itemText);
        }

        // Cập nhật subtotal TextView nếu có (trong layout summary)
        TextView tvSubtotal = cardSummary != null ? cardSummary.findViewById(R.id.tv_subtotal) : null;
        if (tvSubtotal != null) {
            tvSubtotal.setText(formattedAmount);
        }

        // Enable/disable checkout button dựa trên total
        if (btnCheckout != null) {
            btnCheckout.setEnabled(totalItems > 0 && totalAmount > 0);
            if (totalItems == 0) {
                btnCheckout.setText("Giỏ hàng trống");
            } else {
                btnCheckout.setText(String.format("Thanh toán (%s)", formattedAmount));
            }
        }


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