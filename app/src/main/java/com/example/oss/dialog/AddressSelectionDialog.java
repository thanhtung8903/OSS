package com.example.oss.dialog;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.oss.R;
import com.example.oss.activity.AddAddressActivity;
import com.example.oss.adapter.AddressAdapter;
import com.example.oss.entity.Address;
import com.example.oss.viewmodel.AddressViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.ArrayList;
import java.util.List;

public class AddressSelectionDialog extends DialogFragment implements AddressAdapter.OnAddressActionListener {

    private MaterialToolbar toolbar;
    private RecyclerView rvAddresses;
    private View layoutEmptyAddresses;
    private TextView tvEmptyMessage;
    private MaterialButton btnAddNewAddress;

    private AddressViewModel addressViewModel;
    private AddressAdapter addressAdapter;
    private OnAddressSelectedListener listener;
    private Address selectedAddress;

    public interface OnAddressSelectedListener {
        void onAddressSelected(Address address);
    }

    public void setOnAddressSelectedListener(OnAddressSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_OSS);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_address_selection, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupToolbar();
        setupViewModel();
        setupRecyclerView();
        setupListeners();
        loadAddresses();
    }

    private void initViews(View view) {
        toolbar = view.findViewById(R.id.toolbar);
        rvAddresses = view.findViewById(R.id.rv_addresses);
        layoutEmptyAddresses = view.findViewById(R.id.layout_empty_addresses);
        tvEmptyMessage = view.findViewById(R.id.tv_empty_message);
        btnAddNewAddress = view.findViewById(R.id.btn_add_new_address);
    }

    private void setupToolbar() {
        toolbar.setTitle("Chọn địa chỉ giao hàng");
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(v -> dismiss());
    }

    private void setupViewModel() {
        addressViewModel = new ViewModelProvider(this).get(AddressViewModel.class);
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        rvAddresses.setLayoutManager(layoutManager);

        // Sử dụng selection mode cho adapter
        addressAdapter = new AddressAdapter(this, true);
        rvAddresses.setAdapter(addressAdapter);
    }

    private void setupListeners() {
        btnAddNewAddress.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), AddAddressActivity.class);
            startActivity(intent);
            dismiss();
        });
    }

    private void loadAddresses() {
        addressViewModel.getUserAddresses().observe(getViewLifecycleOwner(), addresses -> {
            if (addresses != null) {
                if (addresses.isEmpty()) {
                    showEmptyState();
                } else {
                    showAddressList(addresses);
                }
            }
        });
    }

    private void showEmptyState() {
        rvAddresses.setVisibility(View.GONE);
        layoutEmptyAddresses.setVisibility(View.VISIBLE);
        tvEmptyMessage.setText("Bạn chưa có địa chỉ giao hàng nào.\nThêm địa chỉ mới để tiếp tục.");
    }

    private void showAddressList(List<Address> addresses) {
        layoutEmptyAddresses.setVisibility(View.GONE);
        rvAddresses.setVisibility(View.VISIBLE);
        addressAdapter.updateAddresses(addresses);
    }

    @Override
    public void onAddressEdit(Address address) {
        Intent intent = new Intent(getContext(), AddAddressActivity.class);
        intent.putExtra(AddAddressActivity.EXTRA_ADDRESS_ID, address.getId());
        startActivity(intent);
        dismiss();
    }

    @Override
    public void onAddressDelete(Address address) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Xóa địa chỉ")
                .setMessage("Bạn có chắc chắn muốn xóa địa chỉ này?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    addressViewModel.deleteAddress(address);
                    dialog.dismiss();
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
        selectedAddress = address;
        if (listener != null) {
            listener.onAddressSelected(address);
        }
        dismiss();
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }
}