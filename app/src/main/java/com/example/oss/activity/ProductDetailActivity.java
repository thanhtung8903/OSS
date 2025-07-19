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
import com.example.oss.viewmodel.ReviewViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.LinearLayout;
import com.example.oss.adapter.ReviewAdapter;
import com.example.oss.entity.Review;
import com.example.oss.dialog.WriteReviewDialog;
import com.example.oss.repository.UserRepository;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.List;
import java.util.ArrayList;

public class ProductDetailActivity extends AppCompatActivity {

    public static final String EXTRA_PRODUCT_ID = "product_id";

    private ProductViewModel productViewModel;
    private CartViewModel cartViewModel;
    private WishlistViewModel wishlistViewModel;
    private ReviewViewModel reviewViewModel;
    private SessionManager sessionManager;

    // Views
    private ImageView ivProductImage;
    private TextView tvProductName;
    private TextView tvProductPrice;
    private TextView tvProductDescription;
    private TextView tvStockStatus;
    private MaterialButton btnAddToCart;
    private FloatingActionButton fabWishlist;

    // Review Views
    private MaterialButton btnWriteReview;
    private TextView tvAverageRating;
    private TextView tvReviewCount;
    private ImageView[] summaryStars = new ImageView[5];
    private LinearLayout layoutRatingSummary;
    private LinearLayout layoutNoReviews;
    private RecyclerView rvReviews;

    // Review Data
    private ReviewAdapter reviewAdapter;
    private List<Review> reviews = new ArrayList<>();
    private List<String> userNames = new ArrayList<>();

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

        // Review Views
        btnWriteReview = findViewById(R.id.btn_write_review);
        tvAverageRating = findViewById(R.id.tv_average_rating);
        tvReviewCount = findViewById(R.id.tv_review_count);
        layoutRatingSummary = findViewById(R.id.layout_rating_summary);
        layoutNoReviews = findViewById(R.id.layout_no_reviews);
        rvReviews = findViewById(R.id.rv_reviews);

        // Initialize summary stars
        summaryStars[0] = findViewById(R.id.summary_star_1);
        summaryStars[1] = findViewById(R.id.summary_star_2);
        summaryStars[2] = findViewById(R.id.summary_star_3);
        summaryStars[3] = findViewById(R.id.summary_star_4);
        summaryStars[4] = findViewById(R.id.summary_star_5);

        setupReviewsRecyclerView();
    }

    private void initViewModels() {
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);
        wishlistViewModel = new ViewModelProvider(this).get(WishlistViewModel.class);
        reviewViewModel = new ViewModelProvider(this).get(ReviewViewModel.class);
        sessionManager = SessionManager.getInstance(this);
    }

    private void setupReviewsRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvReviews.setLayoutManager(layoutManager);

        reviewAdapter = new ReviewAdapter(reviews, userNames, new ReviewAdapter.OnReviewActionListener() {
            @Override
            public void onEditReview(Review review) {
                // TODO: Implement edit review functionality
                Toast.makeText(ProductDetailActivity.this, "Chỉnh sửa đánh giá", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDeleteReview(Review review) {
                // TODO: Implement delete review functionality
                Toast.makeText(ProductDetailActivity.this, "Xóa đánh giá", Toast.LENGTH_SHORT).show();
            }
        }, sessionManager);

        rvReviews.setAdapter(reviewAdapter);
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

        // Setup review observers
        setupReviewObservers();
    }

    private void setupReviewObservers() {
        // Observe reviews for this product
        reviewViewModel.getReviewsByProduct(productId).observe(this, reviewsList -> {
            if (reviewsList != null) {
                reviews.clear();
                reviews.addAll(reviewsList);
                reviewAdapter.updateReviews(reviews);
                updateReviewsUI(reviewsList);

                // Load user names for reviews
                loadUserNamesForReviews(reviewsList);
            }
        });

        // Observe average rating
        reviewViewModel.getAverageRatingForProduct(productId).observe(this, averageRating -> {
            if (averageRating != null) {
                updateRatingSummary(averageRating);
            }
        });

        // Observe review count
        reviewViewModel.getReviewCountForProduct(productId).observe(this, count -> {
            if (count != null) {
                updateReviewCount(count);
            }
        });

        // Observe review messages
        reviewViewModel.getSuccessMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                reviewViewModel.clearSuccess();
                // Refresh reviews after successful submission
                reviewViewModel.getReviewsByProduct(productId);
            }
        });

        reviewViewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                // Update button text based on specific error
                if (error.contains("cần mua sản phẩm")) {
                    btnWriteReview.setText("Chưa mua sản phẩm");
                } else if (error.contains("đã đánh giá")) {
                    btnWriteReview.setText("Đã đánh giá");
                }

                // Only show toast for actual errors, not status messages
                if (!error.contains("cần mua sản phẩm") && !error.contains("đã đánh giá")) {
                    Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                }
                reviewViewModel.clearError();
            }
        });

        // Check if user can write review
        if (sessionManager.isLoggedIn()) {
            reviewViewModel.checkCanWriteReview(productId);
            reviewViewModel.getCanWriteReview().observe(this, canWrite -> {
                updateReviewButtonState(canWrite);
            });
        } else {
            btnWriteReview.setEnabled(false);
            btnWriteReview.setText("Đăng nhập để đánh giá");
            btnWriteReview.setAlpha(0.6f);
        }
    }

    private void setupClickListeners() {
        btnAddToCart.setOnClickListener(v -> addToCart());
        fabWishlist.setOnClickListener(v -> toggleWishlist());
        btnWriteReview.setOnClickListener(v -> showWriteReviewDialog());
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

    // Review Helper Methods
    private void updateReviewButtonState(Boolean canWrite) {
        if (canWrite == null)
            return;

        if (canWrite) {
            btnWriteReview.setEnabled(true);
            btnWriteReview.setText("Viết đánh giá");
            btnWriteReview.setAlpha(1.0f);
        } else {
            // Check the last error message to determine button text
            btnWriteReview.setEnabled(false);
            btnWriteReview.setAlpha(0.6f);

            // We'll update text based on error message in error observer
            // Default text when cannot write review
            btnWriteReview.setText("Không thể đánh giá");
        }
    }

    private void showWriteReviewDialog() {
        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, "Vui lòng đăng nhập để đánh giá sản phẩm", Toast.LENGTH_SHORT).show();
            return;
        }

        WriteReviewDialog dialog = WriteReviewDialog.newInstance(productId);
        dialog.setOnReviewSubmittedListener(() -> {
            // Refresh review data after submission
            reviewViewModel.getReviewsByProduct(productId);
            reviewViewModel.checkCanWriteReview(productId);
        });
        dialog.show(getSupportFragmentManager(), "WriteReviewDialog");
    }

    private void updateReviewsUI(List<Review> reviewsList) {
        if (reviewsList.isEmpty()) {
            layoutNoReviews.setVisibility(LinearLayout.VISIBLE);
            layoutRatingSummary.setVisibility(LinearLayout.GONE);
            rvReviews.setVisibility(RecyclerView.GONE);
        } else {
            layoutNoReviews.setVisibility(LinearLayout.GONE);
            layoutRatingSummary.setVisibility(LinearLayout.VISIBLE);
            rvReviews.setVisibility(RecyclerView.VISIBLE);
        }
    }

    private void updateRatingSummary(Double averageRating) {
        if (averageRating != null && averageRating > 0) {
            tvAverageRating.setText(String.format(Locale.getDefault(), "%.1f", averageRating));
            updateSummaryStars(averageRating);
        } else {
            tvAverageRating.setText("0.0");
            updateSummaryStars(0.0);
        }
    }

    private void updateReviewCount(Integer count) {
        if (count != null) {
            String countText = count == 1 ? count + " đánh giá" : count + " đánh giá";
            tvReviewCount.setText(countText);
        } else {
            tvReviewCount.setText("0 đánh giá");
        }
    }

    private void updateSummaryStars(Double rating) {
        for (int i = 0; i < summaryStars.length; i++) {
            if (i < rating.intValue()) {
                summaryStars[i].setImageResource(R.drawable.ic_favorite);
                summaryStars[i].setColorFilter(getResources().getColor(R.color.primary));
            } else {
                summaryStars[i].setImageResource(R.drawable.ic_favorite_border);
                summaryStars[i].setColorFilter(getResources().getColor(android.R.color.darker_gray));
            }
        }
    }

    private void loadUserNamesForReviews(List<Review> reviewsList) {
        // For now, we'll use placeholder names since we don't have a direct user query
        // In a real app, you'd query user details from UserRepository
        userNames.clear();
        for (Review review : reviewsList) {
            userNames.add("Người dùng " + review.getUserId());
        }
        reviewAdapter.updateUserNames(userNames);
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