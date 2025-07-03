package com.example.oss.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.example.oss.R;
import com.example.oss.entity.Category;
import com.example.oss.viewmodel.CategoryManagementViewModel;

import java.util.ArrayList;
import java.util.List;

public class AddEditCategoryActivity extends AppCompatActivity {
    private CategoryManagementViewModel viewModel;
    private EditText etCategoryName;
    private EditText etCategoryDescription;
    private Spinner spinnerParentCategory;
    private Button btnSave;
    private Button btnCancel;

    private String mode;
    private int categoryId = -1;
    private String originalName;
    private String originalDescription;
    private Integer originalParentId;
    
    private List<Category> rootCategories = new ArrayList<>();
    private ArrayAdapter<String> spinnerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_category);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize views
        initViews();

        // Setup ViewModel
        setupViewModel();

        // Get intent data
        getIntentData();

        // Setup click listeners
        setupClickListeners();

        // Load parent categories
        loadParentCategories();

        // Update UI based on mode
        updateUI();
    }

    private void initViews() {
        etCategoryName = findViewById(R.id.etCategoryName);
        etCategoryDescription = findViewById(R.id.etCategoryDescription);
        spinnerParentCategory = findViewById(R.id.spinnerParentCategory);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(CategoryManagementViewModel.class);
    }

    private void getIntentData() {
        Intent intent = getIntent();
        mode = intent.getStringExtra("mode");
        
        if ("edit".equals(mode)) {
            categoryId = intent.getIntExtra("category_id", -1);
            originalName = intent.getStringExtra("category_name");
            originalDescription = intent.getStringExtra("category_description");
            originalParentId = intent.getIntExtra("category_parent_id", -1);
            if (originalParentId == -1) {
                originalParentId = null;
            }
        }
    }

    private void setupClickListeners() {
        btnSave.setOnClickListener(v -> saveCategory());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void loadParentCategories() {
        // Observe root categories
        viewModel.getRootCategories().observe(this, categories -> {
            rootCategories.clear();
            rootCategories.addAll(categories);
            
            // Create spinner adapter
            List<String> categoryNames = new ArrayList<>();
            categoryNames.add("-- Chọn danh mục cha (tùy chọn) --");
            
            for (Category category : categories) {
                categoryNames.add(category.getName());
            }
            
            spinnerAdapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_spinner_item, categoryNames);
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerParentCategory.setAdapter(spinnerAdapter);
            
            // Set selection for edit mode
            if ("edit".equals(mode) && originalParentId != null) {
                for (int i = 0; i < rootCategories.size(); i++) {
                    if (rootCategories.get(i).getId() == originalParentId) {
                        spinnerParentCategory.setSelection(i + 1); // +1 because of the first option
                        break;
                    }
                }
            }
        });
    }

    private void updateUI() {
        if ("add".equals(mode)) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Thêm danh mục mới");
            }
        } else if ("edit".equals(mode)) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Chỉnh sửa danh mục");
            }
            
            // Pre-fill the fields
            etCategoryName.setText(originalName);
            etCategoryDescription.setText(originalDescription);
        }
    }

    private boolean saveCategory() {
        String name = etCategoryName.getText().toString().trim();
        String description = etCategoryDescription.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(name)) {
            etCategoryName.setError("Tên danh mục không được để trống");
            return false;
        }

        if (TextUtils.isEmpty(description)) {
            etCategoryDescription.setError("Mô tả không được để trống");
            return false;
        }

        // Get parent category from spinner
        Integer parentId = null;
        int selectedPosition = spinnerParentCategory.getSelectedItemPosition();
        if (selectedPosition > 0 && selectedPosition <= rootCategories.size()) {
            parentId = rootCategories.get(selectedPosition - 1).getId();
        }

        if ("add".equals(mode)) {
            // Create new category
            if (parentId != null) {
                viewModel.createSubCategory(name, description, parentId);
            } else {
                viewModel.createRootCategory(name, description);
            }
            Toast.makeText(this, "Đã thêm danh mục mới", Toast.LENGTH_SHORT).show();
        } else if ("edit".equals(mode)) {
            Category category = Category.builder()
                    .id(categoryId)
                    .name(name)
                    .description(description)
                    .parentId(parentId)
                    .build();
            viewModel.updateCategory(category);
            Toast.makeText(this, "Đã cập nhật danh mục", Toast.LENGTH_SHORT).show();
        }

        // Return result
        setResult(RESULT_OK);
        finish();
        return true;
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