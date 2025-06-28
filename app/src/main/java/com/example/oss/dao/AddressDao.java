package com.example.oss.dao;

import androidx.room.*;
import androidx.lifecycle.LiveData;
import com.example.oss.entity.Address;
import java.util.List;

@Dao
public interface AddressDao {
    @Query("SELECT * FROM addresses WHERE user_id = :userId ORDER BY is_default DESC, id ASC")
    LiveData<List<Address>> getAddressesByUser(int userId);

    @Query("SELECT * FROM addresses WHERE id = :id")
    LiveData<Address> getAddressById(int id);

    @Query("SELECT * FROM addresses WHERE user_id = :userId AND is_default = 1 LIMIT 1")
    LiveData<Address> getDefaultAddress(int userId);

    @Query("SELECT * FROM addresses WHERE user_id = :userId AND is_default = 1 LIMIT 1")
    Address getDefaultAddressSync(int userId);

    @Query("SELECT * FROM addresses WHERE id = :id")
    Address getAddressByIdSync(int id);

    @Query("SELECT COUNT(*) FROM addresses WHERE user_id = :userId")
    LiveData<Integer> getAddressCount(int userId);

    @Insert
    long insertAddress(Address address);

    @Update
    void updateAddress(Address address);

    @Delete
    void deleteAddress(Address address);

    @Query("DELETE FROM addresses WHERE id = :addressId")
    void deleteAddressById(int addressId);

    @Transaction
    default void setDefaultAddress(int userId, int addressId) {
        // Đầu tiên, bỏ default của tất cả addresses của user
        clearDefaultAddresses(userId);
        // Sau đó set address mới làm default
        setAddressAsDefault(addressId);
    }

    @Query("UPDATE addresses SET is_default = 0 WHERE user_id = :userId")
    void clearDefaultAddresses(int userId);

    @Query("UPDATE addresses SET is_default = 1 WHERE id = :addressId")
    void setAddressAsDefault(int addressId);
}