package com.example.oss.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.oss.R;
import com.example.oss.adapter.CategoryManagementAdapter;
import com.example.oss.entity.Category;
import com.example.oss.viewmodel.CategoryManagementViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryManagementActivity extends AppCompatActivity {
    private CategoryManagementViewModel viewModel;
    private CategoryManagementAdapter adapter;
    private RecyclerView recyclerView;
    private FloatingActionButton fabAddCategory;

    private static final int REQUEST_ADD_CATEGORY = 1001;
    private static final int REQUEST_EDIT_CATEGORY = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_management);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Quản lý danh mục");
        }

        // Initialize views
        recyclerView = findViewById(R.id.recyclerView);
        fabAddCategory = findViewById(R.id.fabAddCategory);

        // Setup RecyclerView
        setupRecyclerView();

        // Setup ViewModel
        setupViewModel();

        // Setup click listeners
        setupClickListeners();

        // Observe data
        observeData();
    }

    private void setupRecyclerView() {
        adapter = new CategoryManagementAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Set click listeners for adapter
        adapter.setOnItemClickListener(new CategoryManagementAdapter.OnItemClickListener() {
            @Override
            public void onEditClick(Category category) {
                openEditCategoryDialog(category);
            }

            @Override
            public void onDeleteClick(Category category) {
                checkAndDeleteCategory(category);
            }

            @Override
            public void onItemClick(Category category) {
                openEditCategoryDialog(category);
            }
        });
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(CategoryManagementViewModel.class);
    }

    private void setupClickListeners() {
        fabAddCategory.setOnClickListener(v -> openAddCategoryDialog());
    }

    private void observeData() {
        viewModel.getAllCategories().observe(this, categories -> {
            adapter.submitList(categories);
        });

        // Load parent category names for display
        viewModel.getRootCategories().observe(this, rootCategories -> {
            Map<Integer, String> parentNames = new HashMap<>();
            for (Category category : rootCategories) {
                parentNames.put(category.getId(), category.getName());
            }
            adapter.setParentCategoryNames(parentNames);
        });
    }

    private void openAddCategoryDialog() {
        Intent intent = new Intent(this, AddEditCategoryActivity.class);
        intent.putExtra("mode", "add");
        startActivityForResult(intent, REQUEST_ADD_CATEGORY);
    }

    private void openEditCategoryDialog(Category category) {
        Intent intent = new Intent(this, AddEditCategoryActivity.class);
        intent.putExtra("mode", "edit");
        intent.putExtra("category_id", category.getId());
        intent.putExtra("category_name", category.getName());
        intent.putExtra("category_description", category.getDescription());
        intent.putExtra("category_parent_id", category.getParentId());
        startActivityForResult(intent, REQUEST_EDIT_CATEGORY);
    }

    private void checkAndDeleteCategory(Category category) {
        // Hiển thị loading nếu cần
        Toast.makeText(this, "Đang kiểm tra...", Toast.LENGTH_SHORT).show();
        
        // Kiểm tra xem category có thể xóa được không (chạy trên background thread)
        viewModel.checkCanDeleteCategory(category.getId(), new CategoryManagementViewModel.DeleteCategoryCallback() {
            @Override
            public void onResult(boolean canDelete, String errorMessage) {
                if (canDelete) {
                    // Có thể xóa
                    showDeleteConfirmationDialog(category);
                } else {
                    // Không thể xóa - hiển thị thông báo lỗi
                    showCannotDeleteDialog(category, errorMessage);
                }
            }
        });
    }

    private void showDeleteConfirmationDialog(Category category) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa danh mục '" + category.getName() + "'?\n\n" +
                           "⚠️ Hành động này không thể hoàn tác!")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    viewModel.deleteCategory(category);
                    Toast.makeText(this, "Đã xóa danh mục '" + category.getName() + "'", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showCannotDeleteDialog(Category category, String errorMessage) {
        new AlertDialog.Builder(this)
                .setTitle("❌ Không thể xóa danh mục")
                .setMessage("Không thể xóa danh mục '" + category.getName() + "'.\n\n" + errorMessage)
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_ADD_CATEGORY) {
                Toast.makeText(this, "Đã thêm danh mục mới", Toast.LENGTH_SHORT).show();
            } else if (requestCode == REQUEST_EDIT_CATEGORY) {
                Toast.makeText(this, "Đã cập nhật danh mục", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_category_management, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (item.getItemId() == R.id.action_refresh) {
            viewModel.refreshData();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 