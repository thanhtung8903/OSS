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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.oss.MainActivity;
import com.example.oss.R;
import com.example.oss.activity.ProductDetailActivity;
import com.example.oss.adapter.WishlistAdapter;
import com.example.oss.entity.Product;
import com.example.oss.viewmodel.WishlistViewModel;
import com.example.oss.viewmodel.CartViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.ArrayList;

public class WishlistFragment extends BaseFragment implements
        WishlistAdapter.OnWishlistItemClickListener,
        WishlistAdapter.OnRemoveFromWishlistListener,
        WishlistAdapter.OnAddToCartFromWishlistListener {

    private RecyclerView rvWishlist;
    private View layoutEmptyState;
    private View layoutNotLoggedIn;
    private TextView tvEmptyMessage;
    private MaterialButton btnLogin, btnBrowseProducts, btnBrowseProductsNotLoggedIn;
    private MaterialButton btnClearWishlist;

    // ViewModels
    private WishlistAdapter wishlistAdapter;
    private WishlistViewModel wishlistViewModel;
    private CartViewModel cartViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wishlist, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViewModels();
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

    private void initViewModels() {
        wishlistViewModel = new ViewModelProvider(this).get(WishlistViewModel.class);
        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);
    }

    private void initViews(View view) {
        rvWishlist = view.findViewById(R.id.rv_wishlist);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);
        layoutNotLoggedIn = view.findViewById(R.id.layout_not_logged_in);
        tvEmptyMessage = view.findViewById(R.id.tv_empty_message);
        btnLogin = view.findViewById(R.id.btn_login);
        btnBrowseProducts = view.findViewById(R.id.btn_browse_products);
        btnBrowseProductsNotLoggedIn = view.findViewById(R.id.btn_browse_products_not_logged_in);
        btnClearWishlist = view.findViewById(R.id.btn_clear_wishlist);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> {
            requireLogin();
        });

        btnBrowseProducts.setOnClickListener(v -> navigateToHome());
        btnBrowseProductsNotLoggedIn.setOnClickListener(v -> navigateToHome());

        btnClearWishlist.setOnClickListener(v -> showClearWishlistDialog());
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        rvWishlist.setLayoutManager(layoutManager);

        wishlistAdapter = new WishlistAdapter(new ArrayList<>(), this, this, this);
        rvWishlist.setAdapter(wishlistAdapter);
    }

    private void setupObservers() {
        // Observe wishlist products
        wishlistViewModel.getWishlistProducts().observe(getViewLifecycleOwner(), products -> {
            if (products != null) {
                if (products.isEmpty()) {
                    showEmptyWishlist();
                } else {
                    showWishlistItems();
                    wishlistAdapter.updateWishlistProducts(products);
                }
            }
        });

        // Observe wishlist count for header
        wishlistViewModel.getWishlistCount().observe(getViewLifecycleOwner(), count -> {
            if (count != null && count > 0) {
                btnClearWishlist.setVisibility(View.VISIBLE);
            } else {
                btnClearWishlist.setVisibility(View.GONE);
            }
        });

        // Observe success messages
        wishlistViewModel.getSuccessMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                wishlistViewModel.clearSuccess();
            }
        });

        // Observe error messages
        wishlistViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                wishlistViewModel.clearError();
            }
        });

        // Observe cart messages
        cartViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                cartViewModel.clearError();
            }
        });

        // Observe loading state
        wishlistViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            // TODO: Show/hide loading indicator if needed
        });
    }

    private void checkLoginAndLoadData() {
        if (isLoggedIn()) {
            showLoggedInUI();
            setupRecyclerView();
            setupObservers();
        } else {
            showNotLoggedInUI();
        }
    }

    // WishlistAdapter.OnWishlistItemClickListener
    @Override
    public void onWishlistItemClick(Product product) {
        // Navigate to Product Detail Activity
        Intent intent = new Intent(getActivity(), ProductDetailActivity.class);
        intent.putExtra(ProductDetailActivity.EXTRA_PRODUCT_ID, product.getId());
        startActivity(intent);
    }

    // WishlistAdapter.OnRemoveFromWishlistListener
    @Override
    public void onRemoveFromWishlist(Product product) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Xóa khỏi danh sách yêu thích")
                .setMessage("Bạn có chắc chắn muốn xóa \"" + product.getName() + "\" khỏi danh sách yêu thích?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    wishlistViewModel.removeFromWishlist(product.getId());
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    // WishlistAdapter.OnAddToCartFromWishlistListener
    @Override
    public void onAddToCartFromWishlist(Product product) {
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

    private void showClearWishlistDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Xóa toàn bộ danh sách yêu thích")
                .setMessage("Bạn có chắc chắn muốn xóa toàn bộ danh sách yêu thích? Hành động này không thể hoàn tác.")
                .setPositiveButton("Xóa tất cả", (dialog, which) -> {
                    wishlistViewModel.clearWishlist();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void navigateToHome() {
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();
            // Navigate to Home tab
            mainActivity.findViewById(R.id.nav_home).performClick();
        }
    }

    private void showNotLoggedInUI() {
        hideAllStates();
        layoutNotLoggedIn.setVisibility(View.VISIBLE);
    }

    private void showLoggedInUI() {
        hideAllStates();
        // RecyclerView will be shown when data is loaded
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