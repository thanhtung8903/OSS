package com.example.oss.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.oss.entity.Address;
import com.example.oss.repository.AddressRepository;
import com.example.oss.util.SessionManager;
import java.util.List;

public class AddressViewModel extends AndroidViewModel {
    private AddressRepository addressRepository;
    private SessionManager sessionManager;

    private MutableLiveData<String> successMessage = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public AddressViewModel(@NonNull Application application) {
        super(application);
        addressRepository = new AddressRepository(application);
        sessionManager = SessionManager.getInstance(application);
    }

    // Getters for LiveData
    public LiveData<String> getSuccessMessage() {
        return successMessage;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    // Address operations
    public LiveData<List<Address>> getUserAddresses() {
        SessionManager.SessionUser user = sessionManager.getCurrentUser();
        if (user != null) {
            return addressRepository.getAddressesByUser(user.getId());
        }
        return new MutableLiveData<>();
    }

    public LiveData<Address> getAddressById(int addressId) {
        return addressRepository.getAddressById(addressId);
    }

    public LiveData<Address> getDefaultAddress() {
        SessionManager.SessionUser user = sessionManager.getCurrentUser();
        if (user != null) {
            return addressRepository.getDefaultAddress(user.getId());
        }
        return new MutableLiveData<>();
    }

    public void addAddress(String receiverName, String phoneNumber, String streetAddress,
            String district, String city, String postalCode,
            String addressType, String notes, boolean isDefault) {

        if (!validateInput(receiverName, phoneNumber, streetAddress, city)) {
            return;
        }

        SessionManager.SessionUser user = sessionManager.getCurrentUser();
        if (user == null) {
            errorMessage.setValue("Vui lòng đăng nhập để thêm địa chỉ");
            return;
        }

        isLoading.setValue(true);

        Address address = Address.builder()
                .userId(user.getId())
                .receiverName(receiverName.trim())
                .phoneNumber(phoneNumber.trim())
                .streetAddress(streetAddress.trim())
                .district(district != null ? district.trim() : "")
                .city(city.trim())
                .postalCode(postalCode != null ? postalCode.trim() : "")
                .addressType(addressType != null ? addressType : "HOME")
                .notes(notes != null ? notes.trim() : "")
                .isDefault(isDefault)
                .build();

        try {
            addressRepository.insertAddress(address);

            if (isDefault) {
                addressRepository.setDefaultAddress(user.getId(), address.getId());
            }

            successMessage.setValue("Thêm địa chỉ thành công");
        } catch (Exception e) {
            errorMessage.setValue("Có lỗi xảy ra khi thêm địa chỉ: " + e.getMessage());
        } finally {
            isLoading.setValue(false);
        }
    }

    public void updateAddress(int addressId, String receiverName, String phoneNumber,
            String streetAddress, String district, String city,
            String postalCode, String addressType, String notes) {

        if (!validateInput(receiverName, phoneNumber, streetAddress, city)) {
            return;
        }

        isLoading.setValue(true);

        try {
            addressRepository.updateAddressInfo(addressId, receiverName.trim(),
                    phoneNumber.trim(), streetAddress.trim(),
                    district != null ? district.trim() : "",
                    city.trim(), postalCode != null ? postalCode.trim() : "",
                    addressType != null ? addressType : "HOME",
                    notes != null ? notes.trim() : "");

            successMessage.setValue("Cập nhật địa chỉ thành công");
        } catch (Exception e) {
            errorMessage.setValue("Có lỗi xảy ra khi cập nhật địa chỉ: " + e.getMessage());
        } finally {
            isLoading.setValue(false);
        }
    }

    public void deleteAddress(Address address) {
        isLoading.setValue(true);

        try {
            addressRepository.deleteAddress(address);
            successMessage.setValue("Xóa địa chỉ thành công");
        } catch (Exception e) {
            errorMessage.setValue("Có lỗi xảy ra khi xóa địa chỉ: " + e.getMessage());
        } finally {
            isLoading.setValue(false);
        }
    }

    public void setDefaultAddress(int addressId) {
        SessionManager.SessionUser user = sessionManager.getCurrentUser();
        if (user == null) {
            errorMessage.setValue("Vui lòng đăng nhập");
            return;
        }

        isLoading.setValue(true);

        try {
            addressRepository.setDefaultAddress(user.getId(), addressId);
            successMessage.setValue("Đã đặt làm địa chỉ mặc định");
        } catch (Exception e) {
            errorMessage.setValue("Có lỗi xảy ra: " + e.getMessage());
        } finally {
            isLoading.setValue(false);
        }
    }

    private boolean validateInput(String receiverName, String phoneNumber,
            String streetAddress, String city) {

        if (receiverName == null || receiverName.trim().isEmpty()) {
            errorMessage.setValue("Tên người nhận không được để trống");
            return false;
        }

        if (phoneNumber == null || !phoneNumber.matches("\\d{10,11}")) {
            errorMessage.setValue("Số điện thoại không hợp lệ (10-11 số)");
            return false;
        }

        if (streetAddress == null || streetAddress.trim().isEmpty()) {
            errorMessage.setValue("Địa chỉ không được để trống");
            return false;
        }

        if (city == null || city.trim().isEmpty()) {
            errorMessage.setValue("Tỉnh/Thành phố không được để trống");
            return false;
        }

        return true;
    }

    // Clear messages
    public void clearSuccess() {
        successMessage.setValue(null);
    }

    public void clearError() {
        errorMessage.setValue(null);
    }
}