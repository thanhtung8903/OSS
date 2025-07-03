package com.example.oss.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.oss.R;
import com.example.oss.adapter.UserManagementAdapter;
import com.example.oss.entity.User;
import com.example.oss.viewmodel.UserManagementViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.widget.ArrayAdapter;

public class UserManagementActivity extends AppCompatActivity {
    private UserManagementViewModel viewModel;
    private UserManagementAdapter adapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_management);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Quản lý tài khoản");
        }

        recyclerView = findViewById(R.id.recyclerView);
        adapter = new UserManagementAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(UserManagementViewModel.class);
        viewModel.getAllUsers().observe(this, users -> adapter.submitList(users));

        adapter.setOnItemClickListener(new UserManagementAdapter.OnItemClickListener() {
            @Override
            public void onDetailClick(User user) {
                Intent intent = new Intent(UserManagementActivity.this, UserDetailActivity.class);
                intent.putExtra("user_id", user.getId());
                startActivity(intent);
            }

            @Override
            public void onEditStatusClick(User user) {
                Intent intent = new Intent(UserManagementActivity.this, EditUserStatusActivity.class);
                intent.putExtra("user_id", user.getId());
                startActivity(intent);
            }
        });

        FloatingActionButton fabAddUser = findViewById(R.id.fab_add_user);
        fabAddUser.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddUserActivity.class);
            startActivity(intent);
        });
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