package com.example.oss.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;

import com.example.oss.R;
import com.example.oss.entity.Category;
import com.example.oss.entity.Product;
import com.example.oss.repository.CategoryRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class AdminProductEditDialog extends DialogFragment {
    private TextInputEditText etName, etDescription, etPrice, etQuantity, etImage;
    private SwitchMaterial switchActive;
    private MaterialButton btnSave, btnCancel;
    private Product productToEdit;
    private OnProductSavedListener listener;
    private Spinner spinnerCategory;
    private List<Category> categoryList = new ArrayList<>();
    private CategoryRepository categoryRepository;

    public interface OnProductSavedListener {
        void onProductSaved(Product product);
    }

    public void setOnProductSavedListener(OnProductSavedListener listener) {
        this.listener = listener;
    }

    public static AdminProductEditDialog newInstance(@Nullable Product product) {
        AdminProductEditDialog dialog = new AdminProductEditDialog();
        Bundle args = new Bundle();
        if (product != null) {
            args.putSerializable("product", (Serializable) product);
        }
        dialog.setArguments(args);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_admin_product_edit, container, false);
        etName = view.findViewById(R.id.et_product_name);
        etDescription = view.findViewById(R.id.et_product_description);
        etPrice = view.findViewById(R.id.et_product_price);
        etQuantity = view.findViewById(R.id.et_product_quantity);
        etImage = view.findViewById(R.id.et_product_image);
        spinnerCategory = view.findViewById(R.id.spinner_product_category);
        switchActive = view.findViewById(R.id.switch_product_active);
        btnSave = view.findViewById(R.id.btn_save);
        btnCancel = view.findViewById(R.id.btn_cancel);
        // Load categories
        categoryRepository = new CategoryRepository(requireActivity().getApplication());
        categoryRepository.getAllCategories().observe(getViewLifecycleOwner(), new Observer<List<Category>>() {
            @Override
            public void onChanged(List<Category> categories) {
                categoryList = categories;
                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, getCategoryNames(categories));
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCategory.setAdapter(adapter);
                // Nếu edit thì chọn đúng category
                if (productToEdit != null) {
                    int pos = getCategoryPositionById(productToEdit.getCategoryId());
                    if (pos >= 0) spinnerCategory.setSelection(pos);
                }
            }
        });
        if (getArguments() != null && getArguments().containsKey("product")) {
            productToEdit = (Product) getArguments().getSerializable("product");
            fillProductData(productToEdit);
        }
        btnSave.setOnClickListener(v -> saveProduct());
        btnCancel.setOnClickListener(v -> dismiss());
        return view;
    }

    private List<String> getCategoryNames(List<Category> categories) {
        List<String> names = new ArrayList<>();
        for (Category c : categories) names.add(c.getName());
        return names;
    }
    private int getCategoryPositionById(int id) {
        for (int i = 0; i < categoryList.size(); i++) {
            if (categoryList.get(i).getId() == id) return i;
        }
        return -1;
    }

    private void fillProductData(Product product) {
        etName.setText(product.getName());
        etDescription.setText(product.getDescription());
        etPrice.setText(product.getPrice() != null ? product.getPrice().toString() : "");
        etQuantity.setText(String.valueOf(product.getStockQuantity()));
        etImage.setText(product.getImageUrl());
        // category sẽ được set khi load xong list
        switchActive.setChecked(product.isActive());
    }

    private void saveProduct() {
        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String description = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";
        String priceStr = etPrice.getText() != null ? etPrice.getText().toString().trim() : "";
        String quantityStr = etQuantity.getText() != null ? etQuantity.getText().toString().trim() : "";
        String image = etImage.getText() != null ? etImage.getText().toString().trim() : "";
        boolean isActive = switchActive.isChecked();
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(priceStr) || TextUtils.isEmpty(quantityStr) || categoryList.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin bắt buộc", Toast.LENGTH_SHORT).show();
            return;
        }
        BigDecimal price = new BigDecimal(priceStr);
        int quantity = Integer.parseInt(quantityStr);
        int categoryId = categoryList.get(spinnerCategory.getSelectedItemPosition()).getId();
        Product product = productToEdit != null ? productToEdit : new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setStockQuantity(quantity);
        product.setImageUrl(image);
        product.setCategoryId(categoryId);
        product.setActive(isActive);
        if (listener != null) listener.onProductSaved(product);
        dismiss();
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setLayout(width, height);
        }
    }
} 