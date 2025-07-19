package com.example.oss.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.oss.R;
import com.example.oss.dao.OrderItemDao;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class OrderManagementOrderDetailAdapter extends RecyclerView.Adapter<OrderManagementOrderDetailAdapter.OrderItemViewHolder> {
    private List<OrderItemDao.OrderItemWithProduct> orderItemList;

    public OrderManagementOrderDetailAdapter(List<OrderItemDao.OrderItemWithProduct> orderItemList) {
        this.orderItemList = orderItemList;
    }

    public void setOrderItemList(List<OrderItemDao.OrderItemWithProduct> newList) {
        this.orderItemList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_management_order_detail, parent, false);
        return new OrderItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderItemViewHolder holder, int position) {
        OrderItemDao.OrderItemWithProduct item = orderItemList.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return orderItemList != null ? orderItemList.size() : 0;
    }

    public static class OrderItemViewHolder extends RecyclerView.ViewHolder{
        private TextView tvProductName, tvQuantityPrice, tvTotalPrice;

        public OrderItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvQuantityPrice = itemView.findViewById(R.id.tvQuantityPrice);
            tvTotalPrice = itemView.findViewById(R.id.tvTotalPrice);
        }

        public void bind(OrderItemDao.OrderItemWithProduct item) {
            String name = item.productName;
            int quantity = item.orderItem.getQuantity();
            double price = item.orderItem.getPrice();
            double total = quantity * price;
            NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

            tvProductName.setText(name);
            tvQuantityPrice.setText(quantity + " x " + nf.format(price));
            tvTotalPrice.setText("Thành tiền: " + nf.format(total));
        }
    }
}
