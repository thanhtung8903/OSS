package com.example.oss.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.oss.R;
import com.example.oss.adapter.CartAdapter;
import com.example.oss.adapter.AddressAdapter;
import com.example.oss.entity.Address;
import com.example.oss.entity.Cart;
import com.example.oss.dao.CartDao;
import com.example.oss.viewmodel.CartViewModel;
import com.example.oss.viewmodel.AddressViewModel;
import com.example.oss.viewmodel.OrderViewModel;
import com.example.oss.util.SessionManager;
import com.example.oss.dialog.AddressSelectionDialog;
import com.example.oss.dialog.PaymentMethodDialog;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CheckoutActivity extends AppCompatActivity implements
        AddressSelectionDialog.OnAddressSelectedListener,
        PaymentMethodDialog.OnPaymentMethodSelectedListener {

    private MaterialToolbar toolbar;
    private LinearProgressIndicator progressIndicator;
    private RecyclerView rvOrderItems;
    private MaterialCardView cardShippingAddress, cardPaymentMethod, cardOrderSummary;
    private TextView tvSelectedAddress, tvSelectedPayment, tvSubtotal, tvShippingFee, tvTotal;
    private MaterialButton btnSelectAddress, btnSelectPayment, btnPlaceOrder;

    private CartViewModel cartViewModel;
    private AddressViewModel addressViewModel;
    private OrderViewModel orderViewModel;
    private SessionManager sessionManager;

    private CartAdapter orderItemsAdapter;
    private Address selectedAddress;
    private String selectedPaymentMethod;
    private double subtotal = 0.0;
    private double shippingFee = 0.0; // Miễn phí vận chuyển
    private double total = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        initViews();
        setupToolbar();
        setupViewModels();
        setupListeners();
        loadData();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        progressIndicator = findViewById(R.id.progress_indicator);
        rvOrderItems = findViewById(R.id.rv_order_items);
        cardShippingAddress = findViewById(R.id.card_shipping_address);
        cardPaymentMethod = findViewById(R.id.card_payment_method);
        cardOrderSummary = findViewById(R.id.card_order_summary);
        tvSelectedAddress = findViewById(R.id.tv_selected_address);
        tvSelectedPayment = findViewById(R.id.tv_selected_payment);
        tvSubtotal = findViewById(R.id.tv_subtotal);
        tvShippingFee = findViewById(R.id.tv_shipping_fee);
        tvTotal = findViewById(R.id.tv_total);
        btnSelectAddress = findViewById(R.id.btn_select_address);
        btnSelectPayment = findViewById(R.id.btn_select_payment);
        btnPlaceOrder = findViewById(R.id.btn_place_order);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Thanh toán");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupViewModels() {
        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);
        addressViewModel = new ViewModelProvider(this).get(AddressViewModel.class);
        orderViewModel = new ViewModelProvider(this).get(OrderViewModel.class);
        sessionManager = SessionManager.getInstance(this);
    }

    private void setupListeners() {
        btnSelectAddress.setOnClickListener(v -> showAddressSelectionDialog());
        btnSelectPayment.setOnClickListener(v -> showPaymentMethodDialog());
        btnPlaceOrder.setOnClickListener(v -> placeOrder());
    }

    private void loadData() {
        showLoading(true);

        // Load cart items để hiển thị trong order summary
        cartViewModel.getCartWithProducts().observe(this, cartItems -> {
            if (cartItems != null) {
                setupOrderItemsRecyclerView(cartItems);
                calculateSubtotal(cartItems);
            }
        });

        // Load default address
        addressViewModel.getUserAddresses().observe(this, addresses -> {
            if (addresses != null && !addresses.isEmpty()) {
                // Tìm địa chỉ mặc định
                Address defaultAddress = addresses.stream()
                        .filter(Address::isDefault)
                        .findFirst()
                        .orElse(addresses.get(0)); // Lấy địa chỉ đầu tiên nếu không có mặc định

                if (selectedAddress == null) {
                    selectAddress(defaultAddress);
                }
            }
            showLoading(false);
        });

        // Observe order creation result
        orderViewModel.getOrderCreationResult().observe(this, result -> {
            showLoading(false);
            if (result != null) {
                if (result.isSuccess()) {
                    showOrderSuccess(result.getOrderId());
                } else {
                    Toast.makeText(this, "Lỗi tạo đơn hàng: " + result.getErrorMessage(),
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void setupOrderItemsRecyclerView(List<CartDao.CartWithProduct> cartItems) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvOrderItems.setLayoutManager(layoutManager);

        // Sử dụng CartAdapter ở chế độ read-only (không cho phép thay đổi số lượng)
        orderItemsAdapter = new CartAdapter(cartItems, null, null); // No callbacks for read-only
        rvOrderItems.setAdapter(orderItemsAdapter);
    }

    private void calculateSubtotal(List<CartDao.CartWithProduct> cartItems) {
        subtotal = cartItems.stream()
                .mapToDouble(item -> item.getPrice().doubleValue() * item.getQuantity())
                .sum();
        updateOrderSummary();
    }

    private void updateOrderSummary() {
        total = subtotal + shippingFee;

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        String formattedSubtotal = currencyFormat.format(subtotal).replace("₫", "").trim() + "₫";
        String formattedTotal = currencyFormat.format(total).replace("₫", "").trim() + "₫";

        tvSubtotal.setText(formattedSubtotal);
        tvShippingFee.setText(shippingFee == 0 ? "Miễn phí" : currencyFormat.format(shippingFee));
        tvTotal.setText(formattedTotal);

        // Enable/disable place order button
        btnPlaceOrder.setEnabled(selectedAddress != null && selectedPaymentMethod != null && total > 0);
    }

    private void showAddressSelectionDialog() {
        AddressSelectionDialog dialog = new AddressSelectionDialog();
        dialog.setOnAddressSelectedListener(this);
        dialog.show(getSupportFragmentManager(), "AddressSelectionDialog");
    }

    private void showPaymentMethodDialog() {
        PaymentMethodDialog dialog = new PaymentMethodDialog();
        dialog.setOnPaymentMethodSelectedListener(this);
        dialog.show(getSupportFragmentManager(), "PaymentMethodDialog");
    }

    @Override
    public void onAddressSelected(Address address) {
        selectAddress(address);
    }

    private void selectAddress(Address address) {
        selectedAddress = address;
        String addressText = String.format("%s\n%s\n%s, %s",
                address.getReceiverName(),
                address.getStreetAddress(),
                address.getDistrict(),
                address.getCity());
        tvSelectedAddress.setText(addressText);
        btnSelectAddress.setText("Thay đổi");
        updateOrderSummary();
    }

    @Override
    public void onPaymentMethodSelected(String paymentMethod) {
        selectedPaymentMethod = paymentMethod;
        String displayText;
        switch (paymentMethod) {
            case "cash":
                displayText = "Thanh toán khi nhận hàng (COD)";
                break;
            case "card":
                displayText = "Thẻ tín dụng/Ghi nợ";
                break;
            case "transfer":
                displayText = "Chuyển khoản ngân hàng";
                break;
            default:
                displayText = paymentMethod;
        }
        tvSelectedPayment.setText(displayText);
        btnSelectPayment.setText("Thay đổi");
        updateOrderSummary();
    }

    private void placeOrder() {
        if (selectedAddress == null) {
            Toast.makeText(this, "Vui lòng chọn địa chỉ giao hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedPaymentMethod == null) {
            Toast.makeText(this, "Vui lòng chọn phương thức thanh toán", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);
        btnPlaceOrder.setEnabled(false);

        // Tạo đơn hàng
        SessionManager.SessionUser currentUser = sessionManager.getLoggedInUser();
        if (currentUser != null) {
            orderViewModel.createOrderFromCart(
                    currentUser.getId(),
                    selectedAddress.getId(),
                    selectedPaymentMethod);
        } else {
            showLoading(false);
            Toast.makeText(this, "Phiên đăng nhập đã hết hạn", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void showOrderSuccess(int orderId) {
        Intent intent = new Intent(this, OrderConfirmationActivity.class);
        intent.putExtra("order_id", orderId);
        intent.putExtra("total_amount", total);
        startActivity(intent);
        finish();
    }

    private void showLoading(boolean show) {
        progressIndicator.setVisibility(show ? View.VISIBLE : View.GONE);
        btnPlaceOrder.setEnabled(!show && selectedAddress != null && selectedPaymentMethod != null);
    }
}