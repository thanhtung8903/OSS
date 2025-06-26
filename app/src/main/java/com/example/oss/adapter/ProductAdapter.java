package com.example.oss.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.oss.R;
import com.example.oss.entity.Product;
import com.example.oss.util.ImageLoader;
import com.google.android.material.button.MaterialButton;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.HashSet;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<Product> products;
    private Set<Integer> wishlistProductIds; // Track wishlist products
    private OnProductClickListener onProductClickListener;
    private OnAddToCartClickListener onAddToCartClickListener;
    private OnWishlistClickListener onWishlistClickListener;

    // Interfaces for click listeners
    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public interface OnAddToCartClickListener {
        void onAddToCartClick(Product product);
    }

    public interface OnWishlistClickListener {
        void onWishlistClick(Product product);
    }

    public ProductAdapter(List<Product> products,
            OnProductClickListener onProductClickListener,
            OnAddToCartClickListener onAddToCartClickListener,
            OnWishlistClickListener onWishlistClickListener) {
        this.products = products != null ? products : new ArrayList<>();
        this.wishlistProductIds = new HashSet<>();
        this.onProductClickListener = onProductClickListener;
        this.onAddToCartClickListener = onAddToCartClickListener;
        this.onWishlistClickListener = onWishlistClickListener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = products.get(position);
        holder.bind(product);
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    // Update data
    public void updateProducts(List<Product> newProducts) {
        this.products.clear();
        if (newProducts != null) {
            this.products.addAll(newProducts);
        }
        notifyDataSetChanged();
    }

    // Update wishlist status
    public void updateWishlistProducts(Set<Integer> wishlistProductIds) {
        this.wishlistProductIds = wishlistProductIds != null ? wishlistProductIds : new HashSet<>();
        notifyDataSetChanged();
    }

    // ViewHolder class
    class ProductViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivProductImage;
        private ImageView ivWishlistIcon;
        private TextView tvProductName;
        private TextView tvProductPrice;
        private MaterialButton btnAddToCart;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.iv_product_image);
            ivWishlistIcon = itemView.findViewById(R.id.iv_wishlist_icon);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvProductPrice = itemView.findViewById(R.id.tv_product_price);
            btnAddToCart = itemView.findViewById(R.id.btn_add_to_cart);
        }

        public void bind(Product product) {
            // Set product name
            tvProductName.setText(product.getName());

            // Format and set price
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            String formattedPrice = formatter.format(product.getPrice());
            formattedPrice = formattedPrice.replace("₫", "").trim() + "₫";
            tvProductPrice.setText(formattedPrice);

            // Load product image với ImageLoader
            ImageLoader.loadProductImage(
                    itemView.getContext(),
                    product.getImageUrl(),
                    ivProductImage);

            // Update wishlist icon
            boolean isInWishlist = wishlistProductIds.contains(product.getId());
            if (isInWishlist) {
                ivWishlistIcon.setImageResource(R.drawable.ic_favorite);
                ivWishlistIcon.setColorFilter(itemView.getContext().getResources().getColor(R.color.primary));
            } else {
                ivWishlistIcon.setImageResource(R.drawable.ic_favorite_border);
                ivWishlistIcon
                        .setColorFilter(itemView.getContext().getResources().getColor(android.R.color.darker_gray));
            }

            // Handle stock status
            if (product.getStockQuantity() <= 0) {
                btnAddToCart.setText("Hết hàng");
                btnAddToCart.setEnabled(false);
            } else {
                btnAddToCart.setText("Thêm vào giỏ");
                btnAddToCart.setEnabled(true);
            }

            // Set click listeners
            itemView.setOnClickListener(v -> {
                if (onProductClickListener != null) {
                    onProductClickListener.onProductClick(product);
                }
            });

            btnAddToCart.setOnClickListener(v -> {
                if (onAddToCartClickListener != null && product.getStockQuantity() > 0) {
                    onAddToCartClickListener.onAddToCartClick(product);
                }
            });

            ivWishlistIcon.setOnClickListener(v -> {
                if (onWishlistClickListener != null) {
                    onWishlistClickListener.onWishlistClick(product);
                }
            });
        }
    }
}