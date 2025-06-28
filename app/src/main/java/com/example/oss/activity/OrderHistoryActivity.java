package com.example.oss.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.oss.R;
import com.example.oss.adapter.OrderAdapter;
import com.example.oss.entity.Order;
import com.example.oss.viewmodel.OrderHistoryViewModel;
import com.example.oss.util.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.button.MaterialButton;
import android.widget.TextView;
import java.util.ArrayList;

public class OrderHistoryActivity extends AppCompatActivity implements OrderAdapter.OnOrderActionListener {

    private MaterialToolbar toolbar;
    private LinearProgressIndicator progressIndicator;
    private RecyclerView rvOrders;
    private View layoutEmptyOrders;
    private TextView tvEmptyMessage;
    private MaterialButton btnStartShopping;

    private OrderHistoryViewModel orderHistoryViewModel;
    private OrderAdapter orderAdapter;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        initViews();
        setupToolbar();
        setupViewModel();
        setupRecyclerView();
        setupListeners();
        loadOrderHistory();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        progressIndicator = findViewById(R.id.progress_indicator);
        rvOrders = findViewById(R.id.rv_orders);
        layoutEmptyOrders = findViewById(R.id.layout_empty_orders);
        tvEmptyMessage = findViewById(R.id.tv_empty_message);
        btnStartShopping = findViewById(R.id.btn_start_shopping);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Lịch sử mua hàng");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupViewModel() {
        orderHistoryViewModel = new ViewModelProvider(this).get(OrderHistoryViewModel.class);
        sessionManager = SessionManager.getInstance(this);
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvOrders.setLayoutManager(layoutManager);

        orderAdapter = new OrderAdapter(new ArrayList<>(), this);
        rvOrders.setAdapter(orderAdapter);
    }

    private void setupListeners() {
        btnStartShopping.setOnClickListener(v -> {
            // Navigate back to home (MainActivity with Home tab)
            finish();
        });
    }

    private void loadOrderHistory() {
        SessionManager.SessionUser currentUser = sessionManager.getLoggedInUser();
        if (currentUser == null) {
            finish();
            return;
        }

        showLoading(true);

        // Observe orders
        orderHistoryViewModel.getUserOrders(currentUser.getId()).observe(this, orders -> {
            showLoading(false);
            if (orders != null) {
                if (orders.isEmpty()) {
                    showEmptyState();
                } else {
                    showOrdersList();
                    orderAdapter.updateOrders(orders);
                }
            }
        });

        // Observe errors
        orderHistoryViewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                // Handle error display
                showEmptyState();
                tvEmptyMessage.setText("Có lỗi xảy ra khi tải dữ liệu: " + error);
            }
        });
    }

    private void showLoading(boolean show) {
        progressIndicator.setVisibility(show ? View.VISIBLE : View.GONE);
        rvOrders.setVisibility(show ? View.GONE : View.VISIBLE);
        layoutEmptyOrders.setVisibility(View.GONE);
    }

    private void showEmptyState() {
        rvOrders.setVisibility(View.GONE);
        layoutEmptyOrders.setVisibility(View.VISIBLE);
        tvEmptyMessage.setText("Bạn chưa có đơn hàng nào.\nHãy khám phá và mua sắm ngay!");
    }

    private void showOrdersList() {
        layoutEmptyOrders.setVisibility(View.GONE);
        rvOrders.setVisibility(View.VISIBLE);
    }

    @Override
    public void onOrderClick(Order order) {
        // Navigate to order detail
        Intent intent = new Intent(this, OrderDetailActivity.class);
        intent.putExtra("order_id", order.getId());
        startActivity(intent);
    }

    @Override
    public void onOrderCancel(Order order) {
        // Show confirmation dialog and cancel order
        orderHistoryViewModel.cancelOrder(order.getId());
    }

    @Override
    public void onOrderReorder(Order order) {
        // Add all items from this order back to cart
        orderHistoryViewModel.reorderItems(order.getId());
    }
}