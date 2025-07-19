package com.example.oss.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.oss.R;
import com.example.oss.adapter.AdminProductAdapter;
import com.example.oss.dialog.AdminProductEditDialog;
import com.example.oss.entity.Product;
import com.example.oss.viewmodel.AdminProductViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;

public class AdminProductManagementFragment extends Fragment {
    private RecyclerView recyclerView;
    private AdminProductAdapter adapter;
    private AdminProductViewModel viewModel;
    private FloatingActionButton fabAddProduct;
    private MaterialToolbar toolbar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_product_management, container, false);
        toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
        recyclerView = view.findViewById(R.id.recycler_admin_products);
        fabAddProduct = view.findViewById(R.id.fab_add_product);
        adapter = new AdminProductAdapter(new ArrayList<>(), this::onEditProduct, this::onDeleteProduct);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        viewModel = new ViewModelProvider(this).get(AdminProductViewModel.class);
        viewModel.getAllProducts().observe(getViewLifecycleOwner(), products -> adapter.updateProducts(products));
        fabAddProduct.setOnClickListener(v -> onAddProduct());
        return view;
    }

    private void onAddProduct() {
        AdminProductEditDialog dialog = AdminProductEditDialog.newInstance(null);
        dialog.setOnProductSavedListener(product -> viewModel.insertProduct(product));
        dialog.show(getParentFragmentManager(), "AddProductDialog");
    }

    private void onEditProduct(int productId) {
        // TODO: Hiển thị dialog sửa sản phẩm
        Product product = findProductById(productId);
        if (product != null) {
            AdminProductEditDialog dialog = AdminProductEditDialog.newInstance(product);
            dialog.setOnProductSavedListener(updatedProduct -> viewModel.updateProduct(updatedProduct));
            dialog.show(getParentFragmentManager(), "EditProductDialog");
        }
    }

    private void onDeleteProduct(int productId) {
        new AlertDialog.Builder(requireContext())
            .setTitle("Xóa sản phẩm")
            .setMessage("Bạn có chắc chắn muốn xóa sản phẩm này?")
            .setPositiveButton("Xóa", (dialog, which) -> {
                Product product = findProductById(productId);
                if (product != null) viewModel.deleteProduct(product);
            })
            .setNegativeButton("Hủy", null)
            .show();
    }


    private Product findProductById(int productId) {
        if (adapter == null) return null;
        for (Product p : adapter.getProducts()) {
            if (p.getId() == productId) return p;
        }
        return null;
    }
} 