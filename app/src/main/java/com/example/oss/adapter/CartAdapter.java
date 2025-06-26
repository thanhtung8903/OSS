package com.example.oss.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.oss.R;
import com.example.oss.dao.CartDao;
import com.google.android.material.button.MaterialButton;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<CartDao.CartWithProduct> cartItems;
    private OnQuantityChangeListener onQuantityChangeListener;
    private OnRemoveClickListener onRemoveClickListener;

    // Interfaces for listeners
    public interface OnQuantityChangeListener {
        void onQuantityChanged(int productId, int newQuantity);
    }

    public interface OnRemoveClickListener {
        void onRemoveClick(int productId);
    }

    public CartAdapter(List<CartDao.CartWithProduct> cartItems,
            OnQuantityChangeListener onQuantityChangeListener,
            OnRemoveClickListener onRemoveClickListener) {
        this.cartItems = cartItems != null ? cartItems : new ArrayList<>();
        this.onQuantityChangeListener = onQuantityChangeListener;
        this.onRemoveClickListener = onRemoveClickListener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartDao.CartWithProduct cartItem = cartItems.get(position);
        holder.bind(cartItem);
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public void updateCartItems(List<CartDao.CartWithProduct> newCartItems) {
        this.cartItems.clear();
        if (newCartItems != null) {
            this.cartItems.addAll(newCartItems);
        }
        notifyDataSetChanged();
    }

    // Method để lấy danh sách cart items hiện tại
    public List<CartDao.CartWithProduct> getCartItems() {
        return new ArrayList<>(cartItems);
    }

    // Method để tính tổng số lượng items
    public int getTotalQuantity() {
        int total = 0;
        for (CartDao.CartWithProduct item : cartItems) {
            total += item.getQuantity();
        }
        return total;
    }

    // Method để tính tổng số items (distinct products)
    public int getTotalItemCount() {
        return cartItems.size();
    }

    // Method để tính tổng tiền
    public BigDecimal getTotalAmount() {
        BigDecimal total = BigDecimal.ZERO;
        for (CartDao.CartWithProduct item : cartItems) {
            if (item.getPrice() != null) {
                BigDecimal itemTotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                total = total.add(itemTotal);
            }
        }
        return total;
    }

    class CartViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivProductImage;
        private TextView tvProductName;
        private TextView tvProductPrice;
        private TextView tvQuantity;
        private MaterialButton btnDecrease;
        private MaterialButton btnIncrease;
        private MaterialButton btnRemove;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.iv_product_image);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvProductPrice = itemView.findViewById(R.id.tv_product_price);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            btnDecrease = itemView.findViewById(R.id.btn_decrease);
            btnIncrease = itemView.findViewById(R.id.btn_increase);
            btnRemove = itemView.findViewById(R.id.btn_remove);
        }

        public void bind(CartDao.CartWithProduct cartItem) {
            // Set product name
            tvProductName.setText(cartItem.getProductName());

            // Format and set price
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            String formattedPrice = formatter.format(cartItem.getTotalPrice());
            formattedPrice = formattedPrice.replace("₫", "").trim() + "₫";
            tvProductPrice.setText(formattedPrice);

            // Set quantity
            tvQuantity.setText(String.valueOf(cartItem.getQuantity()));

            // Set product image (placeholder for now)
            ivProductImage.setImageResource(R.drawable.ic_image_placeholder);

            // Handle quantity controls
            btnDecrease.setEnabled(cartItem.getQuantity() > 1);
            btnIncrease.setEnabled(cartItem.getQuantity() < cartItem.getStockQuantity());

            // Set click listeners
            btnDecrease.setOnClickListener(v -> {
                int newQuantity = cartItem.getQuantity() - 1;
                if (newQuantity >= 1 && onQuantityChangeListener != null) {
                    onQuantityChangeListener.onQuantityChanged(cartItem.getProductId(), newQuantity);
                }
            });

            btnIncrease.setOnClickListener(v -> {
                int newQuantity = cartItem.getQuantity() + 1;
                if (newQuantity <= cartItem.getStockQuantity() && onQuantityChangeListener != null) {
                    onQuantityChangeListener.onQuantityChanged(cartItem.getProductId(), newQuantity);
                }
            });

            btnRemove.setOnClickListener(v -> {
                if (onRemoveClickListener != null) {
                    onRemoveClickListener.onRemoveClick(cartItem.getProductId());
                }
            });
        }
    }
}