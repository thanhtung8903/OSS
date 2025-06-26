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
import com.google.android.material.card.MaterialCardView;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WishlistAdapter extends RecyclerView.Adapter<WishlistAdapter.WishlistViewHolder> {

    private List<Product> wishlistProducts;
    private OnWishlistItemClickListener onItemClickListener;
    private OnRemoveFromWishlistListener onRemoveListener;
    private OnAddToCartFromWishlistListener onAddToCartListener;

    // Interfaces for click listeners
    public interface OnWishlistItemClickListener {
        void onWishlistItemClick(Product product);
    }

    public interface OnRemoveFromWishlistListener {
        void onRemoveFromWishlist(Product product);
    }

    public interface OnAddToCartFromWishlistListener {
        void onAddToCartFromWishlist(Product product);
    }

    public WishlistAdapter(List<Product> wishlistProducts,
            OnWishlistItemClickListener onItemClickListener,
            OnRemoveFromWishlistListener onRemoveListener,
            OnAddToCartFromWishlistListener onAddToCartListener) {
        this.wishlistProducts = wishlistProducts != null ? wishlistProducts : new ArrayList<>();
        this.onItemClickListener = onItemClickListener;
        this.onRemoveListener = onRemoveListener;
        this.onAddToCartListener = onAddToCartListener;
    }

    @NonNull
    @Override
    public WishlistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_wishlist, parent, false);
        return new WishlistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WishlistViewHolder holder, int position) {
        Product product = wishlistProducts.get(position);
        holder.bind(product);
    }

    @Override
    public int getItemCount() {
        return wishlistProducts.size();
    }

    // Update data
    public void updateWishlistProducts(List<Product> newProducts) {
        this.wishlistProducts.clear();
        if (newProducts != null) {
            this.wishlistProducts.addAll(newProducts);
        }
        notifyDataSetChanged();
    }

    // Remove specific item
    public void removeItem(int position) {
        if (position >= 0 && position < wishlistProducts.size()) {
            wishlistProducts.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, wishlistProducts.size());
        }
    }

    // ViewHolder class
    class WishlistViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardView;
        private ImageView ivProductImage;
        private ImageView ivRemoveFromWishlist;
        private TextView tvProductName;
        private TextView tvProductPrice;
        private TextView tvStockStatus;
        private MaterialButton btnAddToCart;

        public WishlistViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_wishlist_item);
            ivProductImage = itemView.findViewById(R.id.iv_product_image);
            ivRemoveFromWishlist = itemView.findViewById(R.id.iv_remove_from_wishlist);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvProductPrice = itemView.findViewById(R.id.tv_product_price);
            tvStockStatus = itemView.findViewById(R.id.tv_stock_status);
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

            // Load product image
            ImageLoader.loadProductImage(
                    itemView.getContext(),
                    product.getImageUrl(),
                    ivProductImage);

            // Handle stock status
            if (product.getStockQuantity() <= 0) {
                tvStockStatus.setText("Hết hàng");
                tvStockStatus
                        .setTextColor(itemView.getContext().getResources().getColor(android.R.color.holo_red_dark));
                btnAddToCart.setText("Hết hàng");
                btnAddToCart.setEnabled(false);
            } else if (product.getStockQuantity() <= 5) {
                tvStockStatus.setText("Còn " + product.getStockQuantity() + " sản phẩm");
                tvStockStatus
                        .setTextColor(itemView.getContext().getResources().getColor(android.R.color.holo_orange_dark));
                btnAddToCart.setText("Thêm vào giỏ");
                btnAddToCart.setEnabled(true);
            } else {
                tvStockStatus.setText("Còn hàng");
                tvStockStatus
                        .setTextColor(itemView.getContext().getResources().getColor(android.R.color.holo_green_dark));
                btnAddToCart.setText("Thêm vào giỏ");
                btnAddToCart.setEnabled(true);
            }

            // Set click listeners
            cardView.setOnClickListener(v -> {
                if (onItemClickListener != null) {
                    onItemClickListener.onWishlistItemClick(product);
                }
            });

            ivRemoveFromWishlist.setOnClickListener(v -> {
                if (onRemoveListener != null) {
                    onRemoveListener.onRemoveFromWishlist(product);
                }
            });

            btnAddToCart.setOnClickListener(v -> {
                if (onAddToCartListener != null && product.getStockQuantity() > 0) {
                    onAddToCartListener.onAddToCartFromWishlist(product);
                }
            });
        }
    }
}