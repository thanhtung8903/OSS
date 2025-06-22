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
import com.google.android.material.button.MaterialButton;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<Product> products;
    private OnProductClickListener onProductClickListener;
    private OnAddToCartClickListener onAddToCartClickListener;

    // Interfaces for click listeners
    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public interface OnAddToCartClickListener {
        void onAddToCartClick(Product product);
    }

    public ProductAdapter(List<Product> products,
            OnProductClickListener onProductClickListener,
            OnAddToCartClickListener onAddToCartClickListener) {
        this.products = products != null ? products : new ArrayList<>();
        this.onProductClickListener = onProductClickListener;
        this.onAddToCartClickListener = onAddToCartClickListener;
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

    // ViewHolder class
    class ProductViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivProductImage;
        private TextView tvProductName;
        private TextView tvProductPrice;
        private MaterialButton btnAddToCart;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.iv_product_image);
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
            // Replace VND with ₫ for better display
            formattedPrice = formattedPrice.replace("₫", "").trim() + "₫";
            tvProductPrice.setText(formattedPrice);

            // Set product image (placeholder for now)
            // TODO: Load actual images using Glide or Picasso
            ivProductImage.setImageResource(R.drawable.ic_image_placeholder);

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
        }
    }
}