package com.example.oss.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.oss.R;
import com.example.oss.bean.OrderData;
import com.example.oss.bean.OrderDisplay;
import com.example.oss.entity.Order;
import com.example.oss.entity.OrderItem;
import com.google.android.material.button.MaterialButton;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderManagementAdapter extends RecyclerView.Adapter<OrderManagementAdapter.OrderManagementViewHolder>{
    private List<OrderDisplay> orders;
    public OrderManagementAdapter(List<OrderDisplay> orders){
        this.orders = orders != null ? orders : new ArrayList<>();
    }

    @NonNull
    @Override
    public OrderManagementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_management, parent, false);
        return new OrderManagementViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderManagementViewHolder holder, int position) {
        OrderDisplay  order = orders.get(position);
        holder.bind(order);
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public void updateData(List<OrderDisplay> newOrders)
    {
        this.orders.clear();
        if (newOrders != null) {
            this.orders.addAll(newOrders);
        }
        notifyDataSetChanged();
    }

    class OrderManagementViewHolder extends RecyclerView.ViewHolder{
        private TextView tvOrderId;
        private TextView tvOrderDate;
        private TextView tvOrderStatus;
        private TextView tvCustomerName;
        private TextView tvOrderItems;
        private TextView tvTotalAmount;
        private TextView tvPaymentMethod;
        private TextView tvItemCount;
        private MaterialButton btnViewDetails;
        private MaterialButton btnUpdateStatus;

        public OrderManagementViewHolder(@NonNull View itemView){
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tv_order_id);
            tvOrderDate = itemView.findViewById(R.id.tv_order_date);
            tvOrderStatus = itemView.findViewById(R.id.tv_order_status);
            tvCustomerName = itemView.findViewById(R.id.tv_customer_name);
            tvOrderItems = itemView.findViewById(R.id.tv_order_items);
            tvTotalAmount = itemView.findViewById(R.id.tv_total_amount);
            tvPaymentMethod = itemView.findViewById(R.id.tv_payment_method);
            tvItemCount = itemView.findViewById(R.id.tv_item_count);
            btnViewDetails = itemView.findViewById(R.id.btn_view_details);
            btnUpdateStatus = itemView.findViewById(R.id.btn_update_status);
        }

        public void bind(OrderDisplay orderDisplay)
        {
            tvOrderId.setText("Mã đơn #" + orderDisplay.orderId);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            tvOrderDate.setText(sdf.format(orderDisplay.orderDate));
            tvOrderStatus.setText(orderDisplay.orderStatus);
            tvCustomerName.setText(orderDisplay.customerName);
            tvOrderItems.setText(orderDisplay.productSummary);
            NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            tvTotalAmount.setText(nf.format(orderDisplay.totalAmount));
            tvPaymentMethod.setText(orderDisplay.paymentMethod);
            tvItemCount.setText(orderDisplay.itemCount + " sản phẩm");

            btnViewDetails.setOnClickListener(v -> {
                // Xử lý xem chi tiết
            });

            btnUpdateStatus.setOnClickListener(v -> {
                // Xử lý cập nhật trạng thái
            });
        }
    }
}
