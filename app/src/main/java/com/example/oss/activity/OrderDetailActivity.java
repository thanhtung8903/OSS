package com.example.oss.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.oss.R;
import com.example.oss.adapter.OrderDetailAdapter;
import com.example.oss.entity.Order;
import com.example.oss.entity.OrderItem;
import com.example.oss.viewmodel.OrderViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.card.MaterialCardView;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import com.example.oss.entity.Address;
import com.example.oss.viewmodel.AddressViewModel;

public class OrderDetailActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private LinearProgressIndicator progressIndicator;
    private MaterialCardView cardOrderInfo;
    private TextView tvOrderId, tvOrderDate, tvOrderStatus, tvShippingAddress,
            tvPaymentMethod, tvSubtotal, tvShippingFee, tvTotalAmount;
    private RecyclerView rvOrderItems;
    private View layoutEmptyItems;

    private OrderViewModel orderViewModel;
    private OrderDetailAdapter orderDetailAdapter;
    private int orderId;
    private AddressViewModel addressViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        orderId = getIntent().getIntExtra("order_id", -1);
        if (orderId == -1) {
            finish();
            return;
        }

        initViews();
        setupToolbar();
        setupViewModel();
        setupRecyclerView();
        loadOrderDetail();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        progressIndicator = findViewById(R.id.progress_indicator);
        cardOrderInfo = findViewById(R.id.card_order_info);
        tvOrderId = findViewById(R.id.tv_order_id);
        tvOrderDate = findViewById(R.id.tv_order_date);
        tvOrderStatus = findViewById(R.id.tv_order_status);
        tvShippingAddress = findViewById(R.id.tv_shipping_address);
        tvPaymentMethod = findViewById(R.id.tv_payment_method);
        tvSubtotal = findViewById(R.id.tv_subtotal);
        tvShippingFee = findViewById(R.id.tv_shipping_fee);
        tvTotalAmount = findViewById(R.id.tv_total_amount);
        rvOrderItems = findViewById(R.id.rv_order_items);
        layoutEmptyItems = findViewById(R.id.layout_empty_items);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Chi tiết đơn hàng");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupViewModel() {
        orderViewModel = new ViewModelProvider(this).get(OrderViewModel.class);
        addressViewModel = new ViewModelProvider(this).get(AddressViewModel.class);
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvOrderItems.setLayoutManager(layoutManager);

        orderDetailAdapter = new OrderDetailAdapter(new ArrayList<>());
        rvOrderItems.setAdapter(orderDetailAdapter);
    }

    private void loadOrderDetail() {
        showLoading(true);

        // Load order info
        orderViewModel.getOrderById(orderId).observe(this, order -> {
            if (order != null) {
                displayOrderInfo(order);
            }
        });

        // Load order items
        orderViewModel.getOrderItems(orderId).observe(this, orderItems -> {
            showLoading(false);
            if (orderItems != null && !orderItems.isEmpty()) {
                showOrderItems();
                orderDetailAdapter.updateOrderItems(orderItems);
            } else {
                showEmptyState();
            }
        });
    }

    private void displayOrderInfo(Order order) {
        tvOrderId.setText(String.format("#%d", order.getId()));

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        tvOrderDate.setText(dateFormat.format(order.getOrderDate()));

        tvOrderStatus.setText(getStatusText(order.getStatus()));
        tvOrderStatus.setBackgroundResource(getStatusBackground(order.getStatus()));

        // Lấy địa chỉ giao hàng từ AddressViewModel
        Integer shippingAddressId = order.getShippingAddressId();
        if (shippingAddressId != null) {
            addressViewModel.getAddressById(shippingAddressId).observe(this, address -> {
                if (address != null) {
                    StringBuilder addressText = new StringBuilder();
                    addressText.append(address.getReceiverName());
                    if (address.getPhoneNumber() != null && !address.getPhoneNumber().isEmpty()) {
                        addressText.append(" - ").append(address.getPhoneNumber());
                    }
                    addressText.append("\n");
                    addressText.append(address.getFullAddress());
                    tvShippingAddress.setText(addressText.toString());
                } else {
                    tvShippingAddress.setText("[Địa chỉ không tồn tại]");
                }
            });
        } else {
            tvShippingAddress.setText("[Không có địa chỉ giao hàng]");
        }

        tvPaymentMethod.setText(order.getPaymentMethod());

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        double subtotal = order.getTotalAmount().doubleValue() - 30000; // Giả sử phí ship 30k
        tvSubtotal.setText(currencyFormat.format(subtotal).replace("₫", "").trim() + "₫");
        tvShippingFee.setText("30,000₫");

        String formattedTotal = currencyFormat.format(order.getTotalAmount()).replace("₫", "").trim() + "₫";
        tvTotalAmount.setText(formattedTotal);
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

    private void showLoading(boolean show) {
        progressIndicator.setVisibility(show ? View.VISIBLE : View.GONE);
        cardOrderInfo.setVisibility(show ? View.GONE : View.VISIBLE);
        rvOrderItems.setVisibility(show ? View.GONE : View.VISIBLE);
        layoutEmptyItems.setVisibility(View.GONE);
    }

    private void showEmptyState() {
        rvOrderItems.setVisibility(View.GONE);
        layoutEmptyItems.setVisibility(View.VISIBLE);
    }

    private void showOrderItems() {
        layoutEmptyItems.setVisibility(View.GONE);
        rvOrderItems.setVisibility(View.VISIBLE);
    }
}