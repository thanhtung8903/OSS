package com.example.oss.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.oss.MainActivity;
import com.example.oss.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import java.text.NumberFormat;
import java.util.Locale;

public class OrderConfirmationActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextView tvOrderId, tvTotalAmount, tvOrderMessage;
    private MaterialButton btnViewOrder, btnContinueShopping;

    private int orderId;
    private double totalAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_confirmation);

        getIntentData();
        initViews();
        setupToolbar();
        setupListeners();
        displayOrderInfo();
    }

    private void getIntentData() {
        Intent intent = getIntent();
        orderId = intent.getIntExtra("order_id", 0);
        totalAmount = intent.getDoubleExtra("total_amount", 0.0);
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvOrderId = findViewById(R.id.tv_order_id);
        tvTotalAmount = findViewById(R.id.tv_total_amount);
        tvOrderMessage = findViewById(R.id.tv_order_message);
        btnViewOrder = findViewById(R.id.btn_view_order);
        btnContinueShopping = findViewById(R.id.btn_continue_shopping);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Đặt hàng thành công");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupListeners() {
        btnViewOrder.setOnClickListener(v -> {
            Intent intent = new Intent(this, OrderDetailActivity.class);
            intent.putExtra("order_id", orderId);
            startActivity(intent);
            finish();
        });

        btnContinueShopping.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void displayOrderInfo() {
        tvOrderId.setText(String.format("Mã đơn hàng: #%d", orderId));

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        String formattedAmount = currencyFormat.format(totalAmount).replace("₫", "").trim() + "₫";
        tvTotalAmount.setText(formattedAmount);

        tvOrderMessage.setText("Đơn hàng của bạn đã được đặt thành công!\n\n" +
                "Chúng tôi sẽ xử lý và giao hàng trong thời gian sớm nhất. " +
                "Bạn có thể theo dõi tình trạng đơn hàng trong mục \"Đơn hàng của tôi\".");
    }

    @Override
    public void onBackPressed() {
        // Redirect to main activity instead of going back to checkout
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}