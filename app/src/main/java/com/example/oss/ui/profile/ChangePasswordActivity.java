package com.example.oss.ui.profile;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.oss.R;
import com.example.oss.util.SessionManager;
import com.example.oss.viewmodel.AuthViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.util.regex.Pattern;

public class ChangePasswordActivity extends AppCompatActivity {

    // UI Components
    private MaterialToolbar toolbar;
    private TextInputLayout tilCurrentPassword, tilNewPassword, tilConfirmPassword;
    private TextInputEditText etCurrentPassword, etNewPassword, etConfirmPassword;
    private MaterialButton btnChangePassword, btnCancel;
    private LinearProgressIndicator progressLoading;
    private MaterialCardView cardPasswordStrength;
    private ProgressBar progressPasswordStrength;
    private TextView tvPasswordStrength;

    // ViewModel và Data
    private AuthViewModel authViewModel;
    private SessionManager sessionManager;

    // Password validation
    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile(".*[A-Z].*");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile(".*[a-z].*");
    private static final Pattern DIGIT_PATTERN = Pattern.compile(".*\\d.*");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern
            .compile(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        initComponents();
        setupToolbar();
        setupTextWatchers();
        setupListeners();
        observeViewModel();
    }

    private void initComponents() {
        // Initialize UI components
        toolbar = findViewById(R.id.toolbar);
        tilCurrentPassword = findViewById(R.id.til_current_password);
        tilNewPassword = findViewById(R.id.til_new_password);
        tilConfirmPassword = findViewById(R.id.til_confirm_password);
        etCurrentPassword = findViewById(R.id.et_current_password);
        etNewPassword = findViewById(R.id.et_new_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnChangePassword = findViewById(R.id.btn_change_password);
        btnCancel = findViewById(R.id.btn_cancel);
        progressLoading = findViewById(R.id.progress_loading);
        cardPasswordStrength = findViewById(R.id.card_password_strength);
        progressPasswordStrength = findViewById(R.id.progress_password_strength);
        tvPasswordStrength = findViewById(R.id.tv_password_strength);

        // Initialize ViewModel và SessionManager
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        sessionManager = SessionManager.getInstance(this);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void setupTextWatchers() {
        // Clear errors khi user typing
        etCurrentPassword.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                tilCurrentPassword.setError(null);
            }
        });

        etNewPassword.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                tilNewPassword.setError(null);
                validatePasswordMatch();
                updatePasswordStrength(s.toString());
            }
        });

        etConfirmPassword.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                tilConfirmPassword.setError(null);
                validatePasswordMatch();
            }
        });
    }

    private void setupListeners() {
        btnChangePassword.setOnClickListener(v -> {
            if (validateInput()) {
                changePassword();
            }
        });

        btnCancel.setOnClickListener(v -> {
            onBackPressed();
        });
    }

    private void observeViewModel() {
        // Observe loading state
        authViewModel.getIsLoading().observe(this, isLoading -> {
            setLoadingState(isLoading != null && isLoading);
        });

        // Observe error messages
        authViewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                authViewModel.clearError();
            }
        });

        // Observe change password success
        authViewModel.getUpdateSuccess().observe(this, success -> {
            if (success != null && success) {
                Toast.makeText(this, "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();
                authViewModel.clearUpdateSuccess();
                setResult(RESULT_OK);
                finish();
            }
        });
    }

    private boolean validateInput() {
        boolean isValid = true;

        // Validate current password
        String currentPassword = etCurrentPassword.getText().toString();
        if (currentPassword.isEmpty()) {
            tilCurrentPassword.setError("Vui lòng nhập mật khẩu hiện tại");
            isValid = false;
        }

        // Validate new password
        String newPassword = etNewPassword.getText().toString();
        if (newPassword.isEmpty()) {
            tilNewPassword.setError("Vui lòng nhập mật khẩu mới");
            isValid = false;
        } else if (newPassword.length() < MIN_PASSWORD_LENGTH) {
            tilNewPassword.setError("Mật khẩu phải có ít nhất " + MIN_PASSWORD_LENGTH + " ký tự");
            isValid = false;
        } else if (newPassword.equals(currentPassword)) {
            tilNewPassword.setError("Mật khẩu mới phải khác mật khẩu hiện tại");
            isValid = false;
        }

        // Validate confirm password
        String confirmPassword = etConfirmPassword.getText().toString();
        if (confirmPassword.isEmpty()) {
            tilConfirmPassword.setError("Vui lòng xác nhận mật khẩu");
            isValid = false;
        } else if (!newPassword.equals(confirmPassword)) {
            tilConfirmPassword.setError("Mật khẩu xác nhận không khớp");
            isValid = false;
        }

        return isValid;
    }

    private void validatePasswordMatch() {
        String newPassword = etNewPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        if (!newPassword.isEmpty() && !confirmPassword.isEmpty()) {
            if (!newPassword.equals(confirmPassword)) {
                tilConfirmPassword.setError("Mật khẩu xác nhận không khớp");
            } else {
                tilConfirmPassword.setError(null);
            }
        }
    }

    private void updatePasswordStrength(String password) {
        if (password.isEmpty()) {
            cardPasswordStrength.setVisibility(View.GONE);
            return;
        }

        cardPasswordStrength.setVisibility(View.VISIBLE);

        int score = calculatePasswordStrength(password);
        progressPasswordStrength.setProgress(score);

        String strengthText;
        int strengthColor;

        if (score < 25) {
            strengthText = "Rất yếu";
            strengthColor = getColor(R.color.error);
        } else if (score < 50) {
            strengthText = "Yếu";
            strengthColor = getColor(R.color.error);
        } else if (score < 75) {
            strengthText = "Trung bình";
            strengthColor = getColor(android.R.color.holo_orange_dark);
        } else {
            strengthText = "Mạnh";
            strengthColor = getColor(android.R.color.holo_green_dark);
        }

        tvPasswordStrength.setText(strengthText);
        tvPasswordStrength.setTextColor(strengthColor);
    }

    private int calculatePasswordStrength(String password) {
        int score = 0;

        // Length bonus
        if (password.length() >= MIN_PASSWORD_LENGTH)
            score += 20;
        if (password.length() >= 8)
            score += 10;
        if (password.length() >= 12)
            score += 10;

        // Character variety bonus
        if (LOWERCASE_PATTERN.matcher(password).matches())
            score += 15;
        if (UPPERCASE_PATTERN.matcher(password).matches())
            score += 15;
        if (DIGIT_PATTERN.matcher(password).matches())
            score += 15;
        if (SPECIAL_CHAR_PATTERN.matcher(password).matches())
            score += 15;

        return Math.min(score, 100);
    }

    private void changePassword() {
        String currentPassword = etCurrentPassword.getText().toString();
        String newPassword = etNewPassword.getText().toString();

        SessionManager.SessionUser currentUser = sessionManager.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Lỗi: Không thể xác định người dùng", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        authViewModel.changePassword(currentUser.getId(), currentPassword, newPassword);
    }

    private void setLoadingState(boolean isLoading) {
        progressLoading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnChangePassword.setEnabled(!isLoading);
        btnCancel.setEnabled(!isLoading);
        btnChangePassword.setText(isLoading ? "Đang xử lý..." : "Đổi mật khẩu");

        // Disable input fields khi loading
        etCurrentPassword.setEnabled(!isLoading);
        etNewPassword.setEnabled(!isLoading);
        etConfirmPassword.setEnabled(!isLoading);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (hasUnsavedChanges()) {
            // TODO: Show confirmation dialog
            showExitConfirmation();
        } else {
            super.onBackPressed();
        }
    }

    private boolean hasUnsavedChanges() {
        return !etCurrentPassword.getText().toString().isEmpty() ||
                !etNewPassword.getText().toString().isEmpty() ||
                !etConfirmPassword.getText().toString().isEmpty();
    }

    private void showExitConfirmation() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Xác nhận")
                .setMessage("Bạn có muốn hủy đổi mật khẩu không?")
                .setPositiveButton("Có", (dialog, which) -> {
                    super.onBackPressed();
                })
                .setNegativeButton("Không", null)
                .show();
    }

    // Helper class for TextWatcher
    private abstract static class SimpleTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
    }
}