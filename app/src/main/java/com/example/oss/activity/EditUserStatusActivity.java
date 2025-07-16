package com.example.oss.activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import com.example.oss.R;
import com.example.oss.entity.User;
import com.example.oss.util.UserStatus;
import com.example.oss.viewmodel.UserManagementViewModel;

public class EditUserStatusActivity extends AppCompatActivity {
    private UserManagementViewModel viewModel;
    private TextView tvName, tvEmail, tvRole, tvPhone;
    private Spinner spinnerStatus;
    private Button btnSave, btnCancel;
    private int userId;
    private String[] statusOptions;
    private String[] statusDisplayNames = {"Active", "Inactive", "Banned"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user_status);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Sửa trạng thái tài khoản");
        }

        tvName = findViewById(R.id.tvUserName);
        tvEmail = findViewById(R.id.tvUserEmail);
        tvRole = findViewById(R.id.tvUserRole);
        tvPhone = findViewById(R.id.tvUserPhone);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);

        // Lấy giá trị enum động
        UserStatus[] statuses = UserStatus.values();
        statusOptions = new String[statuses.length];
        for (int i = 0; i < statuses.length; i++) {
            statusOptions[i] = statuses[i].getValue();
        }
        // Adapter hiển thị tên tiếng Việt
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statusDisplayNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(adapter);

        userId = getIntent().getIntExtra("user_id", -1);
        viewModel = new ViewModelProvider(this).get(UserManagementViewModel.class);
        viewModel.getUserById(userId).observe(this, this::bindUser);

        btnSave.setOnClickListener(v -> saveStatus());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void bindUser(User user) {
        if (user == null) return;
        tvName.setText(user.getFullName());
        tvEmail.setText(user.getEmail());
        tvRole.setText(user.getRole());
        tvPhone.setText(user.getPhoneNumber());
        // Set spinner selection
        if (user.getStatus() != null) {
            for (int i = 0; i < statusOptions.length; i++) {
                if (statusOptions[i].equalsIgnoreCase(user.getStatus())) {
                    spinnerStatus.setSelection(i);
                    break;
                }
            }
        }
    }

    private void saveStatus() {
        int selectedIndex = spinnerStatus.getSelectedItemPosition();
        String newStatus = statusOptions[selectedIndex];
        viewModel.updateUserStatus(userId, newStatus);
        Toast.makeText(this, "Đã cập nhật trạng thái tài khoản", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 