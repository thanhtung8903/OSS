package com.example.oss.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.oss.R;
import com.example.oss.MainActivity;
import com.example.oss.viewmodel.AuthViewModel;
import com.example.oss.util.SecurityUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class RegisterActivity extends AppCompatActivity {

    private AuthViewModel authViewModel;

    // UI Components
    private TextInputLayout tilFullName, tilEmail, tilPhone, tilPassword, tilConfirmPassword;
    private TextInputEditText etFullName, etEmail, etPhone, etPassword, etConfirmPassword;
    private MaterialCheckBox cbTerms;
    private MaterialButton btnRegister;
    private CircularProgressIndicator progressBar;
    private TextView tvError, tvReqLength, tvReqUppercase, tvReqLowercase, tvReqNumber, tvReqSpecial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize ViewModel
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Initialize Views
        initViews();

        // Setup Listeners
        setupListeners();

        // Setup Observers
        setupObservers();
    }

    private void initViews() {
        // TextInputLayouts
        tilFullName = findViewById(R.id.til_full_name);
        tilEmail = findViewById(R.id.til_email);
        tilPhone = findViewById(R.id.til_phone);
        tilPassword = findViewById(R.id.til_password);
        tilConfirmPassword = findViewById(R.id.til_confirm_password);

        // EditTexts
        etFullName = findViewById(R.id.et_full_name);
        etEmail = findViewById(R.id.et_email);
        etPhone = findViewById(R.id.et_phone);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);

        // Other components
        cbTerms = findViewById(R.id.cb_terms);
        btnRegister = findViewById(R.id.btn_register);
        progressBar = findViewById(R.id.progress_bar);
        tvError = findViewById(R.id.tv_error);

        // Password requirement TextViews
        tvReqLength = findViewById(R.id.tv_req_length);
        tvReqUppercase = findViewById(R.id.tv_req_uppercase);
        tvReqLowercase = findViewById(R.id.tv_req_lowercase);
        tvReqNumber = findViewById(R.id.tv_req_number);
        tvReqSpecial = findViewById(R.id.tv_req_special);
    }

    private void setupListeners() {
        // Back button
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Register button
        btnRegister.setOnClickListener(v -> performRegister());

        // Login link
        findViewById(R.id.tv_login).setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        // Terms checkbox
        cbTerms.setOnCheckedChangeListener((buttonView, isChecked) -> validateForm());

        // Text watchers for validation
        addTextWatchers();
    }

    private void addTextWatchers() {
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                validateForm();
                clearErrors();
            }
        };

        etFullName.addTextChangedListener(textWatcher);
        etEmail.addTextChangedListener(textWatcher);
        etPhone.addTextChangedListener(textWatcher);
        etPassword.addTextChangedListener(textWatcher);
        etConfirmPassword.addTextChangedListener(textWatcher);

        // Special password watcher for requirements
        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                updatePasswordRequirements(s.toString());
            }
        });
    }

    private void setupObservers() {
        // Loading state
        authViewModel.getIsLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            btnRegister.setEnabled(!isLoading);
        });

        // Error messages
        authViewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                tvError.setText(error);
                tvError.setVisibility(View.VISIBLE);
            } else {
                tvError.setVisibility(View.GONE);
            }
        });

        // Register success
        authViewModel.getRegisterSuccess().observe(this, success -> {
            if (success) {
                Toast.makeText(this, "Đăng ký thành công! Vui lòng đăng nhập.", Toast.LENGTH_LONG).show();

                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();

                    authViewModel.clearRegisterSuccess();
                }, 1000);
            }
        });
    }

    private void validateForm() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        boolean isValid = !fullName.isEmpty() &&
                !email.isEmpty() &&
                !phone.isEmpty() &&
                !password.isEmpty() &&
                !confirmPassword.isEmpty() &&
                cbTerms.isChecked() &&
                SecurityUtils.isValidEmail(email) &&
                SecurityUtils.isPasswordStrong(password) &&
                password.equals(confirmPassword);

        btnRegister.setEnabled(isValid);
    }

    private void updatePasswordRequirements(String password) {
        int colorGreen = ContextCompat.getColor(this, R.color.success);
        int colorGray = ContextCompat.getColor(this, R.color.on_surface_variant);

        // Length check
        boolean hasLength = password.length() >= 8;
        tvReqLength.setTextColor(hasLength ? colorGreen : colorGray);

        // Uppercase check
        boolean hasUpper = password.matches(".*[A-Z].*");
        tvReqUppercase.setTextColor(hasUpper ? colorGreen : colorGray);

        // Lowercase check
        boolean hasLower = password.matches(".*[a-z].*");
        tvReqLowercase.setTextColor(hasLower ? colorGreen : colorGray);

        // Number check
        boolean hasNumber = password.matches(".*\\d.*");
        tvReqNumber.setTextColor(hasNumber ? colorGreen : colorGray);

        // Special character check
        boolean hasSpecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");
        tvReqSpecial.setTextColor(hasSpecial ? colorGreen : colorGray);
    }

    private void clearErrors() {
        tilFullName.setError(null);
        tilEmail.setError(null);
        tilPhone.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);
        tvError.setVisibility(View.GONE);
        authViewModel.clearError();
    }

    private void performRegister() {
        // Get input values
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        clearErrors();
        if (!validateInputs(fullName, email, phone, password, confirmPassword)) {
            return;
        }
        authViewModel.register(fullName, email, password, phone);
    }

    private boolean validateInputs(String fullName, String email, String phone, String password,
            String confirmPassword) {
        boolean isValid = true;

        // Validate full name
        if (fullName.isEmpty()) {
            tilFullName.setError(getString(R.string.error_field_required));
            isValid = false;
        } else if (fullName.length() < 2) {
            tilFullName.setError("Họ tên phải có ít nhất 2 ký tự");
            isValid = false;
        }

        // Validate email
        if (email.isEmpty()) {
            tilEmail.setError(getString(R.string.error_field_required));
            isValid = false;
        } else if (!SecurityUtils.isValidEmail(email)) {
            tilEmail.setError(getString(R.string.error_invalid_email));
            isValid = false;
        }

        // Validate phone
        if (phone.isEmpty()) {
            tilPhone.setError(getString(R.string.error_field_required));
            isValid = false;
        } else if (!phone.matches("\\d{10,11}")) {
            tilPhone.setError(getString(R.string.error_invalid_phone));
            isValid = false;
        }

        // Validate password
        if (password.isEmpty()) {
            tilPassword.setError(getString(R.string.error_field_required));
            isValid = false;
        } else if (!SecurityUtils.isPasswordStrong(password)) {
            tilPassword.setError(getString(R.string.error_password_too_short));
            isValid = false;
        }

        // Validate confirm password
        if (confirmPassword.isEmpty()) {
            tilConfirmPassword.setError(getString(R.string.error_field_required));
            isValid = false;
        } else if (!password.equals(confirmPassword)) {
            tilConfirmPassword.setError(getString(R.string.error_password_mismatch));
            isValid = false;
        }

        // Check terms
        if (!cbTerms.isChecked()) {
            Toast.makeText(this, "Vui lòng đồng ý với điều khoản sử dụng", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        return isValid;
    }
}