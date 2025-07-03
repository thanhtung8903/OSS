package com.example.oss.activity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.oss.R;
import com.example.oss.entity.User;
import com.example.oss.util.SecurityUtils;
import com.example.oss.viewmodel.UserManagementViewModel;
import java.util.Date;

public class AddUserActivity extends AppCompatActivity {
    private EditText etFullName, etEmail, etPhone, etPassword, etConfirmPassword;
    private Spinner spinnerStatus;
    private Button btnCreate, btnCancel;
    private TextView tvError;
    private UserManagementViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);

        etFullName = findViewById(R.id.et_full_name);
        etEmail = findViewById(R.id.et_email);
        etPhone = findViewById(R.id.et_phone);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        spinnerStatus = findViewById(R.id.spinner_status);
        btnCreate = findViewById(R.id.btn_create_user);
        btnCancel = findViewById(R.id.btn_cancel);
        tvError = findViewById(R.id.tv_error);

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"Active", "Inactive"});
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);

        viewModel = new ViewModelProvider(this).get(UserManagementViewModel.class);

        btnCreate.setOnClickListener(v -> createUser());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void createUser() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();
        String status = (String) spinnerStatus.getSelectedItem();
        tvError.setVisibility(android.view.View.GONE);
        tvError.setText("");
        if (fullName.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            tvError.setText("Vui lòng nhập đầy đủ thông tin");
            tvError.setVisibility(android.view.View.VISIBLE);
            return;
        }
        if (!SecurityUtils.isValidEmail(email)) {
            tvError.setText("Email không hợp lệ");
            tvError.setVisibility(android.view.View.VISIBLE);
            return;
        }
        if (viewModel.isEmailExists(email)) {
            tvError.setText("Email đã tồn tại");
            tvError.setVisibility(android.view.View.VISIBLE);
            return;
        }
        if (!SecurityUtils.isPasswordStrong(password)) {
            tvError.setText("Mật khẩu chưa đủ mạnh");
            tvError.setVisibility(android.view.View.VISIBLE);
            return;
        }
        if (!password.equals(confirmPassword)) {
            tvError.setText("Mật khẩu xác nhận không khớp");
            tvError.setVisibility(android.view.View.VISIBLE);
            return;
        }
        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhoneNumber(phone);
        user.setPassword(SecurityUtils.hashPassword(password));
        user.setRole("Customer");
        user.setStatus(status);
        user.setCreatedAt(new Date());
        viewModel.createUser(user);
        Toast.makeText(this, "Tạo tài khoản thành công", Toast.LENGTH_SHORT).show();
        finish();
    }
} 