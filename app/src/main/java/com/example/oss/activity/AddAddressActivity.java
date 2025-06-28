package com.example.oss.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.oss.R;
import com.example.oss.entity.Address;
import com.example.oss.viewmodel.AddressViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class AddAddressActivity extends AppCompatActivity {

    public static final String EXTRA_ADDRESS_ID = "extra_address_id";

    private AddressViewModel addressViewModel;
    private boolean isEditMode = false;
    private int editAddressId = -1;

    // UI Components
    private MaterialToolbar toolbar;
    private LinearProgressIndicator progressIndicator;
    private TextInputLayout tilReceiverName, tilPhoneNumber, tilStreetAddress,
            tilDistrict, tilCity, tilPostalCode, tilNotes;
    private TextInputEditText etReceiverName, etPhoneNumber, etStreetAddress,
            etDistrict, etCity, etPostalCode, etNotes;
    private AutoCompleteTextView spinnerAddressType;
    private SwitchMaterial switchDefault;
    private MaterialButton btnSave, btnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_address);

        // Check if edit mode
        editAddressId = getIntent().getIntExtra(EXTRA_ADDRESS_ID, -1);
        isEditMode = editAddressId != -1;

        initViews();
        setupToolbar();
        setupAddressTypeSpinner();
        setupViewModel();
        setupObservers();
        setupListeners();

        if (isEditMode) {
            loadAddressForEdit();
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        progressIndicator = findViewById(R.id.progress_indicator);

        // TextInputLayouts
        tilReceiverName = findViewById(R.id.til_receiver_name);
        tilPhoneNumber = findViewById(R.id.til_phone_number);
        tilStreetAddress = findViewById(R.id.til_street_address);
        tilDistrict = findViewById(R.id.til_district);
        tilCity = findViewById(R.id.til_city);
        tilPostalCode = findViewById(R.id.til_postal_code);
        tilNotes = findViewById(R.id.til_notes);

        // EditTexts
        etReceiverName = findViewById(R.id.et_receiver_name);
        etPhoneNumber = findViewById(R.id.et_phone_number);
        etStreetAddress = findViewById(R.id.et_street_address);
        etDistrict = findViewById(R.id.et_district);
        etCity = findViewById(R.id.et_city);
        etPostalCode = findViewById(R.id.et_postal_code);
        etNotes = findViewById(R.id.et_notes);

        // Other components
        spinnerAddressType = findViewById(R.id.spinner_address_type);
        switchDefault = findViewById(R.id.switch_default);
        btnSave = findViewById(R.id.btn_save);
        btnCancel = findViewById(R.id.btn_cancel);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(isEditMode ? "Sửa địa chỉ" : "Thêm địa chỉ mới");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupAddressTypeSpinner() {
        String[] addressTypes = {
                "Nhà riêng",
                "Văn phòng",
                "Khác"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                addressTypes);
        spinnerAddressType.setAdapter(adapter);
        spinnerAddressType.setText(addressTypes[0], false); // Default to "Nhà riêng"
    }

    private void setupViewModel() {
        addressViewModel = new ViewModelProvider(this).get(AddressViewModel.class);
    }

    private void setupObservers() {
        // Observe loading state
        addressViewModel.getIsLoading().observe(this, isLoading -> {
            progressIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            btnSave.setEnabled(!isLoading);
        });

        // Observe success messages
        addressViewModel.getSuccessMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                addressViewModel.clearSuccess();
                finish(); // Close activity on success
            }
        });

        // Observe error messages
        addressViewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                addressViewModel.clearError();
            }
        });
    }

    private void setupListeners() {
        btnSave.setOnClickListener(v -> saveAddress());
        btnCancel.setOnClickListener(v -> onBackPressed());
    }

    private void loadAddressForEdit() {
        addressViewModel.getAddressById(editAddressId).observe(this, address -> {
            if (address != null) {
                populateFields(address);
            }
        });
    }

    private void populateFields(Address address) {
        etReceiverName.setText(address.getReceiverName());
        etPhoneNumber.setText(address.getPhoneNumber());
        etStreetAddress.setText(address.getStreetAddress());
        etDistrict.setText(address.getDistrict());
        etCity.setText(address.getCity());
        etPostalCode.setText(address.getPostalCode());
        etNotes.setText(address.getNotes());

        // Set address type
        String addressType = getAddressTypeDisplayName(address.getAddressType());
        spinnerAddressType.setText(addressType, false);

        switchDefault.setChecked(address.isDefault());
    }

    private void saveAddress() {
        // Clear previous errors
        clearErrors();

        // Get input values
        String receiverName = etReceiverName.getText().toString().trim();
        String phoneNumber = etPhoneNumber.getText().toString().trim();
        String streetAddress = etStreetAddress.getText().toString().trim();
        String district = etDistrict.getText().toString().trim();
        String city = etCity.getText().toString().trim();
        String postalCode = etPostalCode.getText().toString().trim();
        String notes = etNotes.getText().toString().trim();
        String addressTypeDisplay = spinnerAddressType.getText().toString();
        String addressType = getAddressTypeCode(addressTypeDisplay);
        boolean isDefault = switchDefault.isChecked();

        // Validate inputs
        if (!validateInputs(receiverName, phoneNumber, streetAddress, city)) {
            return;
        }

        // Save or update address
        if (isEditMode) {
            addressViewModel.updateAddress(
                    editAddressId,
                    receiverName,
                    phoneNumber,
                    streetAddress,
                    district,
                    city,
                    postalCode,
                    addressType,
                    notes);
        } else {
            addressViewModel.addAddress(
                    receiverName,
                    phoneNumber,
                    streetAddress,
                    district,
                    city,
                    postalCode,
                    addressType,
                    notes,
                    isDefault);
        }
    }

    private boolean validateInputs(String receiverName, String phoneNumber,
            String streetAddress, String city) {
        boolean isValid = true;

        if (TextUtils.isEmpty(receiverName)) {
            tilReceiverName.setError("Tên người nhận không được để trống");
            isValid = false;
        }

        if (TextUtils.isEmpty(phoneNumber)) {
            tilPhoneNumber.setError("Số điện thoại không được để trống");
            isValid = false;
        } else if (!phoneNumber.matches("\\d{10,11}")) {
            tilPhoneNumber.setError("Số điện thoại không hợp lệ (10-11 số)");
            isValid = false;
        }

        if (TextUtils.isEmpty(streetAddress)) {
            tilStreetAddress.setError("Địa chỉ không được để trống");
            isValid = false;
        }

        if (TextUtils.isEmpty(city)) {
            tilCity.setError("Tỉnh/Thành phố không được để trống");
            isValid = false;
        }

        return isValid;
    }

    private void clearErrors() {
        tilReceiverName.setError(null);
        tilPhoneNumber.setError(null);
        tilStreetAddress.setError(null);
        tilDistrict.setError(null);
        tilCity.setError(null);
        tilPostalCode.setError(null);
        tilNotes.setError(null);
    }

    private String getAddressTypeCode(String displayName) {
        switch (displayName) {
            case "Nhà riêng":
                return "HOME";
            case "Văn phòng":
                return "OFFICE";
            case "Khác":
            default:
                return "OTHER";
        }
    }

    private String getAddressTypeDisplayName(String code) {
        switch (code != null ? code.toUpperCase() : "HOME") {
            case "HOME":
                return "Nhà riêng";
            case "OFFICE":
                return "Văn phòng";
            case "OTHER":
            default:
                return "Khác";
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}