package com.example.oss.activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import com.example.oss.R;
import com.example.oss.entity.User;
import com.example.oss.viewmodel.UserManagementViewModel;

public class UserDetailActivity extends AppCompatActivity {
    private UserManagementViewModel viewModel;
    private TextView tvName, tvEmail, tvRole, tvStatus, tvPhone;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Chi tiết tài khoản");
        }

        tvName = findViewById(R.id.tvUserName);
        tvEmail = findViewById(R.id.tvUserEmail);
        tvRole = findViewById(R.id.tvUserRole);
        tvStatus = findViewById(R.id.tvUserStatus);
        tvPhone = findViewById(R.id.tvUserPhone);

        userId = getIntent().getIntExtra("user_id", -1);
        viewModel = new ViewModelProvider(this).get(UserManagementViewModel.class);
        viewModel.getUserById(userId).observe(this, this::bindUser);
    }

    private void bindUser(User user) {
        if (user == null) return;
        tvName.setText(user.getFullName());
        tvEmail.setText(user.getEmail());
        tvRole.setText(user.getRole());
        tvStatus.setText(user.getStatus());
        tvPhone.setText(user.getPhoneNumber());
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