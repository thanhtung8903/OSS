package com.example.oss.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.oss.R;
import com.example.oss.entity.OrderItem;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OrderDetailAdapter extends RecyclerView.Adapter<OrderDetailAdapter.OrderDetailViewHolder> {

    private List<OrderItem> orderItems = new ArrayList<>();

    public OrderDetailAdapter(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    @NonNull
    @Override
    public OrderDetailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_detail, parent, false);
        return new OrderDetailViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderDetailViewHolder holder, int position) {
        OrderItem orderItem = orderItems.get(position);
        holder.bind(orderItem);
    }

    @Override
    public int getItemCount() {
        return orderItems.size();
    }

    public void updateOrderItems(List<OrderItem> newOrderItems) {
        this.orderItems.clear();
        this.orderItems.addAll(newOrderItems);
        notifyDataSetChanged();
    }

    class OrderDetailViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivProductImage;
        private TextView tvProductName, tvProductPrice, tvQuantity, tvTotalPrice;

        public OrderDetailViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.iv_product_image);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvProductPrice = itemView.findViewById(R.id.tv_product_price);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            tvTotalPrice = itemView.findViewById(R.id.tv_total_price);
        }

        public void bind(OrderItem orderItem) {
            // Product name
            String productName = orderItem.getProductName();
            if (productName == null || productName.isEmpty()) {
                productName = "Sản phẩm";
            }
            tvProductName.setText(productName);

            // Product price
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            String formattedPrice = currencyFormat.format(orderItem.getPrice()).replace("₫", "").trim() + "₫";
            tvProductPrice.setText(formattedPrice);

            // Quantity
            tvQuantity.setText(String.format("x%d", orderItem.getQuantity()));

            // Total price for this item
            double totalPrice = orderItem.getPrice() * orderItem.getQuantity();
            String formattedTotal = currencyFormat.format(totalPrice).replace("₫", "").trim() + "₫";
            tvTotalPrice.setText(formattedTotal);

            // Product image - placeholder for now
            ivProductImage.setImageResource(R.drawable.ic_image_placeholder);
        }
    }
}