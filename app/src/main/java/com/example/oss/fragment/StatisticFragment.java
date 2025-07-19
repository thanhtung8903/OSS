package com.example.oss.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.oss.R;
import com.example.oss.database.AppDatabase;
import com.google.android.material.appbar.MaterialToolbar;
import java.util.List;
import java.util.concurrent.Executors;

public class StatisticFragment extends Fragment {
    private MaterialToolbar toolbarStatistic;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistic, container, false);
        toolbarStatistic = view.findViewById(R.id.toolbar_statistic);
        toolbarStatistic.setNavigationOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        TextView tvRevenue = view.findViewById(R.id.tv_revenue);
        TextView tvStock = view.findViewById(R.id.tv_stock);
        TextView tvOrders = view.findViewById(R.id.tv_orders);
        TextView tvProducts = view.findViewById(R.id.tv_products);
        TextView tvCustomers = view.findViewById(R.id.tv_customers);

        Executors.newSingleThreadExecutor().execute(() -> {
            Context context = requireContext().getApplicationContext();
            int customerCount = AppDatabase.getDatabase(context).userDao().getCustomerCountSync();
            int productCount = AppDatabase.getDatabase(context).productDao().getAllActiveProductsSync().size();
            int orderCount = AppDatabase.getDatabase(context).orderDao().getAllOrdersSync().size();
            int categoryCount = AppDatabase.getDatabase(context).categoryDao().getCategoryCountSync();
            int stockQuantity = AppDatabase.getDatabase(context).productDao().getTotalStockQuantity();

            // Tính tổng doanh thu từ các đơn hàng đã hoàn thành/thanh toán
            java.math.BigDecimal totalRevenue = java.math.BigDecimal.ZERO;
            java.util.List<com.example.oss.entity.Order> allOrders = AppDatabase.getDatabase(context).orderDao().getAllOrdersSync();
            for (com.example.oss.entity.Order order : allOrders) {
                String status = order.getStatus();
                if (status != null && (status.equalsIgnoreCase("confirmed") || status.equalsIgnoreCase("shipped") || status.equalsIgnoreCase("delivered") || status.equalsIgnoreCase("đã hoàn thành") || status.equalsIgnoreCase("đã giao"))) {
                    if (order.getTotalAmount() != null) {
                        totalRevenue = totalRevenue.add(order.getTotalAmount());
                    }
                }
            }

            String revenueStr = "Doanh thu: " + totalRevenue.toPlainString() + "đ";
            String stockStr = "Hàng tồn kho: " + stockQuantity;
            String ordersStr = "Tổng đơn hàng: " + orderCount;
            String productsStr = "Tổng sản phẩm: " + productCount;
            String customersStr = "Tổng khách hàng: " + customerCount;

            requireActivity().runOnUiThread(() -> {
                tvRevenue.setText(revenueStr);
                tvStock.setText(stockStr);
                tvOrders.setText(ordersStr);
                tvProducts.setText(productsStr);
                tvCustomers.setText(customersStr);
            });
        });

        return view;
    }
} 