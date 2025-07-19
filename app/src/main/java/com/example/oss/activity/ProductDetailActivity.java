package com.example.oss.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.ViewGroup;
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
import com.example.oss.repository.UserRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.LinearLayout;
import com.example.oss.adapter.ReviewAdapter;
import com.example.oss.entity.Review;
import com.example.oss.entity.User;
import com.example.oss.dialog.WriteReviewDialog;
import com.example.oss.repository.UserRepository;
import com.example.oss.util.SampleDataManager;
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
    private UserRepository userRepository;
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
        userRepository = new UserRepository(getApplication());
        sessionManager = SessionManager.getInstance(this);
    }

    private void setupReviewsRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvReviews.setLayoutManager(layoutManager);

        // Fix nested scrolling issue
        rvReviews.setNestedScrollingEnabled(false);
        rvReviews.setHasFixedSize(false);

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

        android.util.Log.d("ProductDetail", "setupReviewsRecyclerView completed");
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
                android.util.Log.d("ProductDetail",
                        "Loaded " + reviewsList.size() + " reviews for product " + productId);

                // Clear and update reviews list
                reviews.clear();
                reviews.addAll(reviewsList);

                // Update UI state first
                updateReviewsUI(reviewsList);

                if (!reviewsList.isEmpty()) {
                    // Load user names first, then update adapter
                    loadUserNamesForReviews(reviewsList);

                    // Fallback: Show reviews immediately with default user names after 2 seconds
                    new android.os.Handler().postDelayed(() -> {
                        if (userNames.size() != reviewsList.size() && reviewAdapter.getItemCount() == 0) {
                            android.util.Log.w("ProductDetail",
                                    "Fallback: User names not loaded completely and adapter is empty, using defaults");
                            showReviewsWithFallbackUserNames(reviewsList);
                        } else {
                            android.util.Log.d("ProductDetail",
                                    "Fallback not needed - adapter has " + reviewAdapter.getItemCount() + " items");
                        }
                    }, 2000);
                } else {
                    // No reviews, just update adapter
                    reviewAdapter.updateReviews(reviews);
                }
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

        // Debug: Long click on product name to recreate sample data
        tvProductName.setOnLongClickListener(v -> {
            recreateSampleDataForDebug();
            return true;
        });

        // Debug: Click on rating summary to force refresh real reviews data
        layoutRatingSummary.setOnClickListener(v -> {
            forceRefreshReviews();
        });

        // Debug: Double tap on RecyclerView to test with simple data
        rvReviews.setOnClickListener(v -> {
            testWithSimpleData();
        });
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
        android.util.Log.d("ProductDetail", "updateReviewsUI: " + reviewsList.size() + " reviews");

        if (reviewsList.isEmpty()) {
            layoutNoReviews.setVisibility(LinearLayout.VISIBLE);
            layoutRatingSummary.setVisibility(LinearLayout.GONE);
            rvReviews.setVisibility(RecyclerView.GONE);
            android.util.Log.d("ProductDetail", "Showing no reviews layout");
        } else {
            layoutNoReviews.setVisibility(LinearLayout.GONE);
            layoutRatingSummary.setVisibility(LinearLayout.VISIBLE);
            rvReviews.setVisibility(RecyclerView.VISIBLE);
            android.util.Log.d("ProductDetail", "Showing reviews: rating summary + recycler view");

            // Force RecyclerView to measure properly
            setRecyclerViewHeight(rvReviews, reviewsList.size());

            // Debug: Log each review
            for (int i = 0; i < reviewsList.size(); i++) {
                Review review = reviewsList.get(i);
                android.util.Log.d("ProductDetail", "Review " + i + ": " +
                        "rating=" + review.getRating() + ", " +
                        "comment='" + review.getComment() + "', " +
                        "userId=" + review.getUserId() + ", " +
                        "date=" + review.getCreatedAt());
            }
        }
    }

    private void setRecyclerViewHeight(RecyclerView recyclerView, int itemCount) {
        if (itemCount > 0) {
            // Estimate height per item (you may need to adjust this)
            int estimatedItemHeight = (int) (120 * getResources().getDisplayMetrics().density); // ~120dp
            int totalHeight = estimatedItemHeight * itemCount;

            ViewGroup.LayoutParams params = recyclerView.getLayoutParams();
            params.height = totalHeight;
            recyclerView.setLayoutParams(params);

            android.util.Log.d("ProductDetail",
                    "Set RecyclerView height to: " + totalHeight + " for " + itemCount + " items");
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
        userNames.clear();

        android.util.Log.d("ProductDetail", "Loading user names for " + reviewsList.size() + " reviews");

        // Load real user names from database
        new Thread(() -> {
            for (Review review : reviewsList) {
                try {
                    User user = userRepository.getUserByIdSync(review.getUserId());
                    if (user != null) {
                        userNames.add(user.getFullName());
                        android.util.Log.d("ProductDetail",
                                "Loaded user name: " + user.getFullName() + " for userId: " + review.getUserId());
                    } else {
                        userNames.add("Người dùng " + review.getUserId());
                        android.util.Log.d("ProductDetail", "No user found for userId: " + review.getUserId());
                    }
                } catch (Exception e) {
                    userNames.add("Người dùng " + review.getUserId());
                    android.util.Log.e("ProductDetail", "Error loading user for userId: " + review.getUserId(), e);
                }
            }

            // Update UI on main thread
            runOnUiThread(() -> {
                android.util.Log.d("ProductDetail",
                        "Updating adapter with " + reviews.size() + " reviews and " + userNames.size() + " user names");

                // Update adapter with both reviews and user names (create new lists to avoid
                // reference issues)
                List<Review> reviewsCopy = new ArrayList<>(reviews);
                List<String> userNamesCopy = new ArrayList<>(userNames);

                android.util.Log.d("ProductDetail",
                        "Creating copies - reviews: " + reviewsCopy.size() + ", userNames: " + userNamesCopy.size());
                reviewAdapter.updateReviewsAndUserNames(reviewsCopy, userNamesCopy);

                // Force RecyclerView refresh
                rvReviews.post(() -> {
                    android.util.Log.d("ProductDetail",
                            "Final RecyclerView adapter itemCount: " + reviewAdapter.getItemCount());
                    android.util.Log.d("ProductDetail", "Final RecyclerView visibility: " + rvReviews.getVisibility());
                    android.util.Log.d("ProductDetail", "Final RecyclerView height: " + rvReviews.getHeight());

                    reviewAdapter.notifyDataSetChanged();
                    rvReviews.requestLayout();
                    rvReviews.invalidate();
                });
            });
        }).start();
    }

    // Debug method to recreate sample data
    private void recreateSampleDataForDebug() {
        Toast.makeText(this, "Đang recreate sample data... Check logs", Toast.LENGTH_LONG).show();

        SampleDataManager sampleDataManager = new SampleDataManager(this);
        sampleDataManager.recreateSampleData();

        // Refresh current product data after a delay
        new android.os.Handler().postDelayed(() -> {
            if (productViewModel != null) {
                productViewModel.getProductById(productId).observe(this, product -> {
                    if (product != null) {
                        displayProductInfo(product);
                    }
                });

                // Refresh reviews
                reviewViewModel.getReviewsByProduct(productId);
            }
            Toast.makeText(this, "Sample data recreated! Reviews should now display.", Toast.LENGTH_LONG).show();
        }, 3000);
    }

    // Method to force refresh real reviews data
    private void forceRefreshReviews() {
        android.util.Log.d("ProductDetail", "Force refreshing real reviews data");
        Toast.makeText(this, "Refreshing reviews...", Toast.LENGTH_SHORT).show();

        // Clear current data
        reviews.clear();
        userNames.clear();
        reviewAdapter.updateReviews(new ArrayList<>());

        // Remove all observers to avoid conflicts
        reviewViewModel.getReviewsByProduct(productId).removeObservers(this);
        reviewViewModel.getAverageRatingForProduct(productId).removeObservers(this);
        reviewViewModel.getReviewCountForProduct(productId).removeObservers(this);

        // Wait a moment then re-setup observer
        new android.os.Handler().postDelayed(() -> {
            setupReviewObservers();
        }, 100);

        android.util.Log.d("ProductDetail", "Reviews refresh initiated");
    }

    // Fallback method to show reviews with default user names
    private void showReviewsWithFallbackUserNames(List<Review> reviewsList) {
        android.util.Log.d("ProductDetail", "Using fallback user names for " + reviewsList.size() + " reviews");

        // Create fallback user names
        List<String> fallbackUserNames = new ArrayList<>();
        for (Review review : reviewsList) {
            fallbackUserNames.add("Người dùng " + review.getUserId());
        }

        // Update adapter with fallback names (create copies to avoid reference issues)
        List<Review> reviewsCopy = new ArrayList<>(reviews);
        List<String> fallbackUserNamesCopy = new ArrayList<>(fallbackUserNames);

        android.util.Log.d("ProductDetail",
                "Fallback copies - reviews: " + reviewsCopy.size() + ", userNames: " + fallbackUserNamesCopy.size());
        reviewAdapter.updateReviewsAndUserNames(reviewsCopy, fallbackUserNamesCopy);

        // Force RecyclerView refresh
        rvReviews.post(() -> {
            android.util.Log.d("ProductDetail", "Fallback RecyclerView refresh completed");
            reviewAdapter.notifyDataSetChanged();
            rvReviews.requestLayout();
            rvReviews.invalidate();
        });

        Toast.makeText(this, "Hiển thị " + reviewsList.size() + " đánh giá", Toast.LENGTH_SHORT).show();
    }

    // Simple test method with hardcoded data
    private void testWithSimpleData() {
        android.util.Log.d("ProductDetail", "Testing with simple hardcoded data");

        // Create simple test data
        List<Review> testReviews = new ArrayList<>();
        testReviews.add(new Review(1, productId, 5, "Test comment 1"));
        testReviews.add(new Review(2, productId, 4, "Test comment 2"));

        List<String> testUserNames = new ArrayList<>();
        testUserNames.add("Test User 1");
        testUserNames.add("Test User 2");

        // Force show
        layoutNoReviews.setVisibility(LinearLayout.GONE);
        layoutRatingSummary.setVisibility(LinearLayout.VISIBLE);
        rvReviews.setVisibility(RecyclerView.VISIBLE);
        setRecyclerViewHeight(rvReviews, testReviews.size());

        // Update adapter
        reviewAdapter.updateReviewsAndUserNames(testReviews, testUserNames);

        // Force layout
        rvReviews.post(() -> {
            android.util.Log.d("ProductDetail", "Test data - adapter itemCount: " + reviewAdapter.getItemCount());
            reviewAdapter.notifyDataSetChanged();
            rvReviews.requestLayout();
        });

        Toast.makeText(this, "Loaded test data", Toast.LENGTH_SHORT).show();
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