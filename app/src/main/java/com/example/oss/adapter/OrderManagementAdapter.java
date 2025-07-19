package com.example.oss.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.oss.R;
import com.example.oss.bean.OrderData;
import com.example.oss.bean.OrderDisplay;
import com.example.oss.dialog.UpdateOrderManagementStatusDialog;
import com.example.oss.entity.Order;
import com.example.oss.entity.OrderItem;
import com.example.oss.fragment.OrderDetailManagementFragment;
import com.example.oss.repository.OrderRepository;
import com.example.oss.viewmodel.OrderManagementViewModel;
import com.google.android.material.button.MaterialButton;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderManagementAdapter extends RecyclerView.Adapter<OrderManagementAdapter.OrderManagementViewHolder> {
    private List<OrderDisplay> orders;
    public OrderManagementAdapter(List<OrderDisplay> orders) {
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
        OrderDisplay order = orders.get(position);
        holder.bind(order);
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public void updateData(List<OrderDisplay> newOrders) {
        this.orders.clear();
        if (newOrders != null) {
            this.orders.addAll(newOrders);
        }
        notifyDataSetChanged();
    }

    class OrderManagementViewHolder extends RecyclerView.ViewHolder {
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

        public OrderManagementViewHolder(@NonNull View itemView) {
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

        public void bind(OrderDisplay orderDisplay) {
            tvOrderId.setText("Mã đơn #" + orderDisplay.orderId);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            tvOrderDate.setText(sdf.format(orderDisplay.orderDate));
            tvOrderStatus.setText(getStatusDisplay(orderDisplay.orderStatus));
            tvCustomerName.setText(orderDisplay.customerName);
            tvOrderItems.setText(orderDisplay.productSummary);
            NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            tvTotalAmount.setText(nf.format(orderDisplay.totalAmount));
            tvPaymentMethod.setText(orderDisplay.paymentMethod);
            tvItemCount.setText(orderDisplay.itemCount + " sản phẩm");

            btnViewDetails.setOnClickListener(v -> {
                Activity activity = getActivityFromView(v);
                if (activity instanceof FragmentActivity) {
                    FragmentActivity fragmentActivity = (FragmentActivity) activity;

                    OrderDetailManagementFragment fragment = OrderDetailManagementFragment.newInstance(orderDisplay);

                    fragmentActivity.getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, fragment)  // ID của container để gắn fragment
                            .addToBackStack(null)  // Để quay lại bằng nút Back
                            .commit();
                }
            });

            btnUpdateStatus.setOnClickListener(v -> {
                Activity activity = getActivityFromView(v);
                if (activity instanceof FragmentActivity) {

                    FragmentActivity fragmentActivity = (FragmentActivity) activity;

                    // Lấy ViewModel
                    OrderManagementViewModel viewModel = new ViewModelProvider(fragmentActivity)
                            .get(OrderManagementViewModel.class);

                    UpdateOrderManagementStatusDialog dialog = new UpdateOrderManagementStatusDialog(orderDisplay, (order, newStatus, note) -> {
                        viewModel.updateOrderStatus(order.orderId, getStatusCodeFromDisplay(newStatus));

                        order.orderStatus = getStatusCodeFromDisplay(newStatus);
                        int position = getBindingAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            notifyItemChanged(position);
                        }
                    });
                    dialog.show(((FragmentActivity) activity).getSupportFragmentManager(), "UpdateStatusDialog");
                }
            });
        }

        private Activity getActivityFromView(View view) {
            Context context = view.getContext();
            while (context instanceof ContextWrapper) {
                if (context instanceof Activity) {
                    return (Activity) context;
                }
                context = ((ContextWrapper) context).getBaseContext();
            }
            return null;
        }

        public String getStatusDisplay(String statusCode) {
            switch (statusCode) {
                case OrderRepository.STATUS_PENDING: return "Chờ xử lý";
                case OrderRepository.STATUS_CONFIRMED: return "Đang xử lý";
                case OrderRepository.STATUS_SHIPPED: return "Đã giao hàng";
                case OrderRepository.STATUS_DELIVERED: return "Hoàn thành";
                case OrderRepository.STATUS_CANCELLED: return "Đã hủy";
                default: return "Không rõ";
            }
        }

        public String getStatusCodeFromDisplay(String displayText) {
            switch (displayText) {
                case "Chờ xử lý": return OrderRepository.STATUS_PENDING;
                case "Đang xử lý": return OrderRepository.STATUS_CONFIRMED;
                case "Đã giao hàng": return OrderRepository.STATUS_SHIPPED;
                case "Hoàn thành": return OrderRepository.STATUS_DELIVERED;
                case "Đã hủy": return OrderRepository.STATUS_CANCELLED;
                default: return OrderRepository.STATUS_PENDING; // fallback
            }
        }
    }
}
