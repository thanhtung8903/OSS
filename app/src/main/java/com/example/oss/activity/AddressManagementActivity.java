package com.example.oss.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.oss.R;
import com.example.oss.adapter.AddressAdapter;
import com.example.oss.entity.Address;
import com.example.oss.viewmodel.AddressViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class AddressManagementActivity extends AppCompatActivity
        implements AddressAdapter.OnAddressActionListener {

    private AddressViewModel addressViewModel;
    private AddressAdapter addressAdapter;
    private RecyclerView rvAddresses;
    private FloatingActionButton fabAddAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_management);

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupViewModel();
        setupObservers();
    }

    private void initViews() {
        rvAddresses = findViewById(R.id.rv_addresses);
        fabAddAddress = findViewById(R.id.fab_add_address);

        fabAddAddress.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddAddressActivity.class);
            startActivity(intent);
        });
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Quản lý địa chỉ");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        addressAdapter = new AddressAdapter(this);
        rvAddresses.setLayoutManager(new LinearLayoutManager(this));
        rvAddresses.setAdapter(addressAdapter);
    }

    private void setupViewModel() {
        addressViewModel = new ViewModelProvider(this).get(AddressViewModel.class);
    }

    private void setupObservers() {
        // Observe addresses
        addressViewModel.getUserAddresses().observe(this, addresses -> {
            if (addresses != null) {
                addressAdapter.updateAddresses(addresses);
            }
        });

        // Observe success messages
        addressViewModel.getSuccessMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                addressViewModel.clearSuccess();
            }
        });

        // Observe error messages
        addressViewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                addressViewModel.clearError();
            }
        });

        // Observe loading state
        addressViewModel.getIsLoading().observe(this, isLoading -> {
            // TODO: Show/hide progress indicator
        });
    }

    @Override
    public void onAddressEdit(Address address) {
        Intent intent = new Intent(this, AddAddressActivity.class);
        intent.putExtra(AddAddressActivity.EXTRA_ADDRESS_ID, address.getId());
        startActivity(intent);
    }

    @Override
    public void onAddressDelete(Address address) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa địa chỉ này?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    addressViewModel.deleteAddress(address);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onAddressSetDefault(Address address) {
        addressViewModel.setDefaultAddress(address.getId());
    }

    @Override
    public void onAddressSelect(Address address) {
        // Not used in management mode
    }
}