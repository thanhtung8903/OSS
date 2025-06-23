package com.example.oss.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.oss.R;
import com.example.oss.entity.User;
import com.example.oss.util.SessionManager;
import com.example.oss.viewmodel.AuthViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.util.regex.Pattern;

public class EditProfileActivity extends AppCompatActivity {

    // UI Components
    private MaterialToolbar toolbar;
    private ImageView ivProfileAvatar;
    private MaterialButton btnChangeAvatar;
    private TextInputLayout tilFullName, tilEmail, tilPhone;
    private TextInputEditText etFullName, etEmail, etPhone;
    private MaterialButton btnSave, btnChangePassword;
    private LinearProgressIndicator progressLoading;

    // ViewModel và Data
    private AuthViewModel authViewModel;
    private SessionManager sessionManager;
    private SessionManager.SessionUser currentUser;

    // Validation patterns
    private static final Pattern PHONE_PATTERN = Pattern.compile("^(\\+84|0)[0-9]{9,10}$");

    // Request codes
    private static final int CHANGE_PASSWORD_REQUEST = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        initComponents();
        setupToolbar();
        setupTextWatchers();
        setupListeners();
        loadUserData();
        observeViewModel();
    }

    private void initComponents() {
        // Initialize UI components
        toolbar = findViewById(R.id.toolbar);
        ivProfileAvatar = findViewById(R.id.iv_profile_avatar);
        btnChangeAvatar = findViewById(R.id.btn_change_avatar);
        tilFullName = findViewById(R.id.til_full_name);
        tilEmail = findViewById(R.id.til_email);
        tilPhone = findViewById(R.id.til_phone);
        etFullName = findViewById(R.id.et_full_name);
        etEmail = findViewById(R.id.et_email);
        etPhone = findViewById(R.id.et_phone);
        btnSave = findViewById(R.id.btn_save);
        btnChangePassword = findViewById(R.id.btn_change_password);
        progressLoading = findViewById(R.id.progress_loading);

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
        etFullName.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                tilFullName.setError(null);
            }
        });

        etPhone.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                tilPhone.setError(null);
            }
        });
    }

    private void setupListeners() {
        btnChangeAvatar.setOnClickListener(v -> {
            // TODO: Implement avatar change functionality
            Toast.makeText(this, "Tính năng đổi ảnh đại diện sẽ có trong phiên bản sau",
                    Toast.LENGTH_SHORT).show();
        });

        btnSave.setOnClickListener(v -> {
            if (validateInput()) {
                saveProfile();
            }
        });

        btnChangePassword.setOnClickListener(v -> {
            Intent intent = new Intent(EditProfileActivity.this, ChangePasswordActivity.class);
            startActivityForResult(intent, CHANGE_PASSWORD_REQUEST);
        });
    }

    private void loadUserData() {
        currentUser = sessionManager.getCurrentUser();
        if (currentUser != null) {
            etFullName.setText(currentUser.getFullName());
            etEmail.setText(currentUser.getEmail());
            etPhone.setText(currentUser.getPhoneNumber());
        } else {
            Toast.makeText(this, "Lỗi: Không thể tải thông tin người dùng", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void observeViewModel() {
        // Observe loading state
        authViewModel.getIsLoading().observe(this, isLoading -> {
            setLoadingState(isLoading);
        });

        // Observe error messages
        authViewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                authViewModel.clearError();
            }
        });

        // Observe update success
        authViewModel.getUpdateSuccess().observe(this, success -> {
            if (success != null && success) {
                Toast.makeText(this, "Cập nhật thông tin thành công!", Toast.LENGTH_SHORT).show();
                // Refresh session data
                sessionManager.updateSession();
                setResult(RESULT_OK);
                finish();
            }
        });
    }

    private boolean validateInput() {
        boolean isValid = true;

        // Validate full name
        String fullName = etFullName.getText().toString().trim();
        if (fullName.isEmpty()) {
            tilFullName.setError("Vui lòng nhập họ và tên");
            isValid = false;
        } else if (fullName.length() < 2) {
            tilFullName.setError("Họ và tên phải có ít nhất 2 ký tự");
            isValid = false;
        }

        // Validate phone number
        String phone = etPhone.getText().toString().trim();
        if (!phone.isEmpty() && !PHONE_PATTERN.matcher(phone).matches()) {
            tilPhone.setError("Số điện thoại không hợp lệ");
            isValid = false;
        }

        return isValid;
    }

    private void saveProfile() {
        String fullName = etFullName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        // Create updated user object
        User updatedUser = User.builder()
                .id(currentUser.getId())
                .email(currentUser.getEmail())
                .fullName(fullName)
                .phoneNumber(phone.isEmpty() ? null : phone)
                .role(currentUser.getRole().toString())
                .status(currentUser.getStatus().toString())
                .build();

        // Update profile via ViewModel (without password)
        authViewModel.updateProfile(updatedUser);
    }

    private void setLoadingState(boolean isLoading) {
        progressLoading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnSave.setEnabled(!isLoading);
        btnSave.setText(isLoading ? "Đang lưu..." : "Lưu thay đổi");
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CHANGE_PASSWORD_REQUEST && resultCode == RESULT_OK) {
            Toast.makeText(this, "Mật khẩu đã được thay đổi thành công", Toast.LENGTH_SHORT).show();
        }
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