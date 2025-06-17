package com.example.oss.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.example.oss.database.AppDatabase;
import com.example.oss.dao.AddressDao;
import com.example.oss.entity.Address;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class AddressRepository {
    private AddressDao addressDao;
    private ExecutorService executor;

    public AddressRepository(Application application) {
        AppDatabase database = AppDatabase.getDatabase(application);
        addressDao = database.addressDao();
        executor = Executors.newFixedThreadPool(2);
    }

    // Read operations
    public LiveData<List<Address>> getAddressesByUser(int userId) {
        return addressDao.getAddressesByUser(userId);
    }

    public LiveData<Address> getAddressById(int id) {
        return addressDao.getAddressById(id);
    }

    public LiveData<Address> getDefaultAddress(int userId) {
        return addressDao.getDefaultAddress(userId);
    }

    public Future<Address> getDefaultAddressSync(int userId) {
        return executor.submit(() -> addressDao.getDefaultAddressSync(userId));
    }

    public LiveData<Integer> getAddressCount(int userId) {
        return addressDao.getAddressCount(userId);
    }

    // Write operations
    public void insertAddress(Address address) {
        executor.execute(() -> addressDao.insertAddress(address));
    }

    public void updateAddress(Address address) {
        executor.execute(() -> addressDao.updateAddress(address));
    }

    public void deleteAddress(Address address) {
        executor.execute(() -> addressDao.deleteAddress(address));
    }

    public void deleteAddressById(int addressId) {
        executor.execute(() -> addressDao.deleteAddressById(addressId));
    }

    public void setDefaultAddress(int userId, int addressId) {
        executor.execute(() -> addressDao.setDefaultAddress(userId, addressId));
    }

    // Business logic methods
    public void addAddress(int userId, String receiverName, String phoneNumber,
            String streetAddress, String city, String postalCode, boolean isDefault) {
        executor.execute(() -> {
            Address address = Address.builder()
                    .userId(userId)
                    .receiverName(receiverName)
                    .phoneNumber(phoneNumber)
                    .streetAddress(streetAddress)
                    .city(city)
                    .postalCode(postalCode)
                    .isDefault(isDefault)
                    .build();

            long addressId = addressDao.insertAddress(address);

            // Nếu đây là địa chỉ mặc định, cần cập nhật các địa chỉ khác
            if (isDefault && addressId > 0) {
                addressDao.setDefaultAddress(userId, (int) addressId);
            }
        });
    }

    public void updateAddressInfo(int addressId, String receiverName, String phoneNumber,
            String streetAddress, String city, String postalCode) {
        executor.execute(() -> {
            Address address = addressDao.getAddressById(addressId).getValue();
            if (address != null) {
                address.setReceiverName(receiverName);
                address.setPhoneNumber(phoneNumber);
                address.setStreetAddress(streetAddress);
                address.setCity(city);
                address.setPostalCode(postalCode);
                addressDao.updateAddress(address);
            }
        });
    }

    public void makeAddressDefault(int userId, int addressId) {
        executor.execute(() -> addressDao.setDefaultAddress(userId, addressId));
    }

    public Future<Boolean> hasAddresses(int userId) {
        return executor.submit(() -> {
            Integer count = addressDao.getAddressCount(userId).getValue();
            return count != null && count > 0;
        });
    }

    public Future<Boolean> isValidAddress(String streetAddress, String city, String postalCode) {
        return executor.submit(() -> {
            // Basic validation
            return streetAddress != null && !streetAddress.trim().isEmpty() &&
                    city != null && !city.trim().isEmpty() &&
                    postalCode != null && postalCode.matches("\\d{5,6}"); // 5-6 digits postal code
        });
    }

    public Future<String> getFormattedAddress(int addressId) {
        return executor.submit(() -> {
            Address address = addressDao.getAddressById(addressId).getValue();
            if (address != null) {
                return String.format("%s\n%s, %s %s\nTel: %s",
                        address.getReceiverName(),
                        address.getStreetAddress(),
                        address.getCity(),
                        address.getPostalCode(),
                        address.getPhoneNumber());
            }
            return "";
        });
    }

    // Validation methods
    public boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber != null && phoneNumber.matches("\\d{10,11}"); // 10-11 digits
    }

    public boolean isValidReceiverName(String name) {
        return name != null && name.trim().length() >= 2;
    }
}