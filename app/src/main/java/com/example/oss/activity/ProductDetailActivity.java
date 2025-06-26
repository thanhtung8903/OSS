package com.example.oss.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.oss.R;
import com.example.oss.entity.Product;
import com.example.oss.util.ImageLoader;
import com.example.oss.util.SessionManager;
import com.example.oss.viewmodel.ProductViewModel;
import com.example.oss.viewmodel.CartViewModel;
import com.example.oss.viewmodel.WishlistViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.NumberFormat;
import java.util.Locale;

public class ProductDetailActivity extends AppCompatActivity {

    public static final String EXTRA_PRODUCT_ID = "product_id";
    
    private ProductViewModel productViewModel;
    private CartViewModel cartViewModel;
    private WishlistViewModel wishlistViewModel;
    private SessionManager sessionManager;
    
    // Views
    private ImageView ivProductImage;
    private TextView tvProductName;
    private TextView tvProductPrice;
    private TextView tvProductDescription;
    private TextView tvStockStatus;
    private MaterialButton btnAddToCart;
    private FloatingActionButton fabWishlist;
    
    private Product currentProduct;
    private int productId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        // Get product ID from intent
        productId = getIntent().getIntExtra(EXTRA_PRODUCT_ID, -1);
        if (productId == -1) {
            Toast.makeText(this, "Lỗi: Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        initViewModels();
        setupToolbar();
        setupObservers();
        setupClickListeners();
    }

    private void initViews() {
        ivProductImage = findViewById(R.id.iv_product_image);
        tvProductName = findViewById(R.id.tv_product_name);
        tvProductPrice = findViewById(R.id.tv_product_price);
        tvProductDescription = findViewById(R.id.tv_product_description);
        tvStockStatus = findViewById(R.id.tv_stock_status);
        btnAddToCart = findViewById(R.id.btn_add_to_cart);
        fabWishlist = findViewById(R.id.fab_wishlist);
    }

    private void initViewModels() {
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);
        wishlistViewModel = new ViewModelProvider(this).get(WishlistViewModel.class);
        sessionManager = SessionManager.getInstance(this);
    }

    private void setupToolbar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Chi tiết sản phẩm");
        }
    }

    private void setupObservers() {
        // Observe product data
        productViewModel.getProductById(productId).observe(this, product -> {
            if (product != null) {
                currentProduct = product;
                displayProductInfo(product);
            }
        });
        
        // Observe wishlist status for this product
        if (sessionManager.isLoggedIn()) {
            wishlistViewModel.isProductInWishlist(productId).observe(this, isInWishlist -> {
                if (isInWishlist != null) {
                    updateWishlistFab(isInWishlist);
                }
            });

            // Observe wishlist messages
            wishlistViewModel.getSuccessMessage().observe(this, message -> {
                if (message != null && !message.isEmpty()) {
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                    wishlistViewModel.clearSuccess();
                }
            });

            wishlistViewModel.getErrorMessage().observe(this, error -> {
                if (error != null && !error.isEmpty()) {
                    Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                    wishlistViewModel.clearError();
                }
            });
        }
    }

    private void setupClickListeners() {
        btnAddToCart.setOnClickListener(v -> addToCart());
        fabWishlist.setOnClickListener(v -> toggleWishlist());
    }

    private void displayProductInfo(Product product) {
        // Load product image
        ImageLoader.loadProductImage(this, product.getImageUrl(), ivProductImage);
        
        // Set basic info
        tvProductName.setText(product.getName());
        tvProductDescription.setText(product.getDescription());
        
        // Format and set price
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        String formattedPrice = formatter.format(product.getPrice());
        formattedPrice = formattedPrice.replace("₫", "").trim() + "₫";
        tvProductPrice.setText(formattedPrice);
        
        // Set stock status
        if (product.getStockQuantity() <= 0) {
            tvStockStatus.setText("Hết hàng");
            tvStockStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            btnAddToCart.setText("Hết hàng");
            btnAddToCart.setEnabled(false);
        } else if (product.getStockQuantity() <= 5) {
            tvStockStatus.setText("Còn " + product.getStockQuantity() + " sản phẩm");
            tvStockStatus.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
            btnAddToCart.setText("Thêm vào giỏ");
            btnAddToCart.setEnabled(true);
        } else {
            tvStockStatus.setText("Còn hàng");
            tvStockStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            btnAddToCart.setText("Thêm vào giỏ");
            btnAddToCart.setEnabled(true);
        }
    }

    private void addToCart() {
        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, "Vui lòng đăng nhập để thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentProduct != null && currentProduct.getStockQuantity() > 0) {
            SessionManager.SessionUser currentUser = sessionManager.getCurrentUser();
            cartViewModel.addToCart(currentProduct.getId(), 1);
            Toast.makeText(this, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateWishlistFab(boolean isInWishlist) {
        if (isInWishlist) {
            fabWishlist.setImageResource(R.drawable.ic_favorite);
            fabWishlist.setColorFilter(getResources().getColor(R.color.primary));
        } else {
            fabWishlist.setImageResource(R.drawable.ic_favorite_border);
            fabWishlist.setColorFilter(getResources().getColor(android.R.color.darker_gray));
        }
    }

    private void toggleWishlist() {
        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, "Vui lòng đăng nhập để sử dụng danh sách yêu thích", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentProduct != null) {
            wishlistViewModel.toggleWishlist(currentProduct.getId());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}