package com.example.oss.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.oss.R;
import com.example.oss.entity.Order;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private List<Order> orders = new ArrayList<>();
    private OnOrderActionListener listener;

    public interface OnOrderActionListener {
        void onOrderClick(Order order);

        void onOrderCancel(Order order);

        void onOrderReorder(Order order);
    }

    public OrderAdapter(List<Order> orders, OnOrderActionListener listener) {
        this.orders = orders;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orders.get(position);
        holder.bind(order);
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public void updateOrders(List<Order> newOrders) {
        this.orders.clear();
        this.orders.addAll(newOrders);
        notifyDataSetChanged();
    }

    class OrderViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardOrder;
        private TextView tvOrderId, tvOrderDate, tvOrderStatus, tvTotalAmount, tvItemCount;
        private ImageView ivOrderStatus;
        private MaterialButton btnCancel, btnReorder, btnViewDetail;
        private View layoutActions;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            cardOrder = (MaterialCardView) itemView;
            tvOrderId = itemView.findViewById(R.id.tv_order_id);
            tvOrderDate = itemView.findViewById(R.id.tv_order_date);
            tvOrderStatus = itemView.findViewById(R.id.tv_order_status);
            tvTotalAmount = itemView.findViewById(R.id.tv_total_amount);
            tvItemCount = itemView.findViewById(R.id.tv_item_count);
            ivOrderStatus = itemView.findViewById(R.id.iv_order_status);
            btnCancel = itemView.findViewById(R.id.btn_cancel);
            btnReorder = itemView.findViewById(R.id.btn_reorder);
            btnViewDetail = itemView.findViewById(R.id.btn_view_detail);
            layoutActions = itemView.findViewById(R.id.layout_actions);
        }

        public void bind(Order order) {
            // Order ID
            tvOrderId.setText(String.format("#%d", order.getId()));

            // Order Date
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            tvOrderDate.setText(dateFormat.format(order.getOrderDate()));

            // Order Status
            String statusText = getStatusText(order.getStatus());
            tvOrderStatus.setText(statusText);
            tvOrderStatus.setBackgroundResource(getStatusBackground(order.getStatus()));

            // Status Icon
            ivOrderStatus.setImageResource(getStatusIcon(order.getStatus()));
            ivOrderStatus.setColorFilter(getStatusColor(order.getStatus()));

            // Total Amount
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            String formattedAmount = currencyFormat.format(order.getTotalAmount()).replace("₫", "").trim() + "₫";
            tvTotalAmount.setText(formattedAmount);

            // TODO: Get actual item count from OrderItems
            tvItemCount.setText("Xem chi tiết");

            // Action buttons visibility based on status
            setupActionButtons(order);

            // Click listeners
            cardOrder.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onOrderClick(order);
                }
            });

            btnCancel.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onOrderCancel(order);
                }
            });

            btnReorder.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onOrderReorder(order);
                }
            });

            btnViewDetail.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onOrderClick(order);
                }
            });
        }

        private void setupActionButtons(Order order) {
            String status = order.getStatus();

            // Cancel button - only show for pending and confirmed orders
            btnCancel.setVisibility(
                    ("pending".equals(status) || "confirmed".equals(status)) ? View.VISIBLE : View.GONE);

            // Reorder button - show for delivered and cancelled orders
            btnReorder.setVisibility(
                    ("delivered".equals(status) || "cancelled".equals(status)) ? View.VISIBLE : View.GONE);
        }

        private String getStatusText(String status) {
            switch (status.toLowerCase()) {
                case "pending":
                    return "Chờ xác nhận";
                case "confirmed":
                    return "Đã xác nhận";
                case "shipped":
                    return "Đang giao";
                case "delivered":
                    return "Đã giao";
                case "cancelled":
                    return "Đã hủy";
                default:
                    return "Không xác định";
            }
        }

        private int getStatusBackground(String status) {
            switch (status.toLowerCase()) {
                case "pending":
                    return R.drawable.status_pending_background;
                case "confirmed":
                    return R.drawable.status_confirmed_background;
                case "shipped":
                    return R.drawable.status_shipped_background;
                case "delivered":
                    return R.drawable.status_delivered_background;
                case "cancelled":
                    return R.drawable.status_cancelled_background;
                default:
                    return R.drawable.address_type_background;
            }
        }

        private int getStatusIcon(String status) {
            switch (status.toLowerCase()) {
                case "pending":
                    return R.drawable.ic_schedule;
                case "confirmed":
                    return R.drawable.ic_check_circle;
                case "shipped":
                    return R.drawable.ic_local_shipping;
                case "delivered":
                    return R.drawable.ic_check_circle;
                case "cancelled":
                    return R.drawable.ic_cancel;
                default:
                    return R.drawable.ic_help;
            }
        }

        private int getStatusColor(String status) {
            switch (status.toLowerCase()) {
                case "pending":
                    return R.color.warning;
                case "confirmed":
                    return R.color.primary;
                case "shipped":
                    return R.color.info;
                case "delivered":
                    return R.color.success;
                case "cancelled":
                    return R.color.error;
                default:
                    return R.color.on_surface_variant;
            }
        }
    }
}