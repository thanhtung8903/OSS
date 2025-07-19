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
import java.util.List;
import java.util.Locale;

public class AdminProductAdapter extends RecyclerView.Adapter<AdminProductAdapter.AdminProductViewHolder> {
    private List<Product> products;
    private final OnEditClickListener onEditClickListener;
    private final OnDeleteClickListener onDeleteClickListener;

    public interface OnEditClickListener {
        void onEditClick(int productId);
    }
    public interface OnDeleteClickListener {
        void onDeleteClick(int productId);
    }


    // Trong AdminProductAdapter.java
    public List<Product> getProducts() {
        return products;
    }

    public AdminProductAdapter(List<Product> products, OnEditClickListener editListener, OnDeleteClickListener deleteListener) {
        this.products = products;
        this.onEditClickListener = editListener;
        this.onDeleteClickListener = deleteListener;
    }

    @NonNull
    @Override
    public AdminProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_product, parent, false);
        return new AdminProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminProductViewHolder holder, int position) {
        holder.bind(products.get(position));
    }

    @Override
    public int getItemCount() {
        return products != null ? products.size() : 0;
    }

    public void updateProducts(List<Product> newProducts) {
        this.products = newProducts;
        notifyDataSetChanged();
    }

    class AdminProductViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivProductImage;
        private final TextView tvProductName;
        private final TextView tvProductPrice;
        private final MaterialButton btnEdit;
        private final MaterialButton btnDelete;
        private final TextView tvProductStatus;
        public AdminProductViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.iv_product_image);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvProductPrice = itemView.findViewById(R.id.tv_product_price);
            btnEdit = itemView.findViewById(R.id.btn_edit_product);
            tvProductStatus = itemView.findViewById(R.id.tv_product_status);
            btnDelete = itemView.findViewById(R.id.btn_delete_product);
        }
        public void bind(Product product) {
            tvProductName.setText(product.getName());
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            String formattedPrice = formatter.format(product.getPrice());
            formattedPrice = formattedPrice.replace("₫", "").trim() + "₫";
            tvProductPrice.setText(formattedPrice);
            ImageLoader.loadProductImage(itemView.getContext(), product.getImageUrl(), ivProductImage);
            if (product.isActive()) {
                tvProductStatus.setText("Active");
                tvProductStatus.setTextColor(androidx.core.content.ContextCompat.getColor(itemView.getContext(), R.color.green));
            } else {
                tvProductStatus.setText("Inactive");
                tvProductStatus.setTextColor(androidx.core.content.ContextCompat.getColor(itemView.getContext(), R.color.red));
            }
            btnEdit.setOnClickListener(v -> onEditClickListener.onEditClick(product.getId()));
            btnDelete.setOnClickListener(v -> onDeleteClickListener.onDeleteClick(product.getId()));
        }
    }
} 