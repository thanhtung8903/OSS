package com.example.oss.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import com.example.oss.R;
import com.example.oss.viewmodel.AuthViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.example.oss.util.SecurityUtils;
import android.util.Log;

public class ForgotPasswordActivity extends AppCompatActivity {

    private static final String TAG = "ForgotPasswordActivity";

    // UI Components
    private ImageButton btnBack;
    private TextInputLayout tilEmail;
    private TextInputEditText etEmail;
    private MaterialButton btnSendResetEmail;
    private TextView tvError, tvSuccess, tvBackToLogin;
    private LinearProgressIndicator progressIndicator;

    // ViewModel
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.sv_forgot_password), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Log.d(TAG, "onCreate called");

        // Initialize ViewModel
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        initViews();
        setupListeners();
        observeViewModel();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        tilEmail = findViewById(R.id.til_email);
        etEmail = findViewById(R.id.et_email);
        btnSendResetEmail = findViewById(R.id.btn_send_reset_email);
        tvError = findViewById(R.id.tv_error);
        tvSuccess = findViewById(R.id.tv_success);
        tvBackToLogin = findViewById(R.id.tv_back_to_login);
        progressIndicator = findViewById(R.id.progress_indicator);
    }

    private void setupListeners() {
        // Back button
        btnBack.setOnClickListener(v -> {
            Log.d(TAG, "Back button clicked");
            finish();
        });

        // Send reset email button
        btnSendResetEmail.setOnClickListener(v -> {
            Log.d(TAG, "Send reset email button clicked");
            sendResetEmail();
        });

        // Back to login
        tvBackToLogin.setOnClickListener(v -> {
            Log.d(TAG, "Back to login clicked");
            navigateToLogin();
        });
    }

    private void observeViewModel() {
        // Observe loading state
        authViewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading != null) {
                updateLoadingState(isLoading);
            }
        });

        // Observe forgot password result
        authViewModel.getForgotPasswordResult().observe(this, result -> {
            if (result != null) {
                handleForgotPasswordResult(result);
            }
        });

        // Observe error messages
        authViewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                showError(errorMessage);
            }
        });
    }

    private void sendResetEmail() {
        String email = etEmail.getText().toString().trim();

        // Clear previous messages
        clearMessages();

        // Validate input
        if (!validateEmail(email)) {
            return;
        }

        // Call ViewModel to send reset email
        authViewModel.sendResetPasswordEmail(email);
    }

    private boolean validateEmail(String email) {
        if (email.isEmpty()) {
            showError("Vui lòng nhập email");
            tilEmail.setError("Email không được để trống");
            return false;
        }

        if (!SecurityUtils.isValidEmail(email)) {
            showError("Email không hợp lệ");
            tilEmail.setError("Định dạng email không đúng");
            return false;
        }

        tilEmail.setError(null);
        return true;
    }

    private void updateLoadingState(boolean isLoading) {
        btnSendResetEmail.setEnabled(!isLoading);
        btnSendResetEmail.setText(isLoading ? "Đang gửi..." : "Gửi email đặt lại");
        progressIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);

        // Disable email input while loading
        etEmail.setEnabled(!isLoading);
    }

    private void handleForgotPasswordResult(AuthViewModel.ForgotPasswordResult result) {
        if (result.isSuccess()) {
            showSuccess("Email đặt lại mật khẩu đã được gửi đến " + etEmail.getText().toString().trim() +
                    ". Vui lòng kiểm tra hộp thư và làm theo hướng dẫn.");

            // Optionally navigate back to login after a delay
            etEmail.postDelayed(() -> {
                if (!isFinishing()) {
                    navigateToLogin();
                }
            }, 3000); // 3 seconds delay

        } else {
            showError(result.getErrorMessage());
        }
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
        tvSuccess.setVisibility(View.GONE);
    }

    private void showSuccess(String message) {
        tvSuccess.setText(message);
        tvSuccess.setVisibility(View.VISIBLE);
        tvError.setVisibility(View.GONE);
    }

    private void clearMessages() {
        tvError.setVisibility(View.GONE);
        tvSuccess.setVisibility(View.GONE);
        tilEmail.setError(null);
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        navigateToLogin();
    }
}