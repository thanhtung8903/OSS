package com.example.oss.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.Transformations;

import com.example.oss.bean.OrderData;
import com.example.oss.repository.OrderRepository;
import com.example.oss.util.SearchFilter;

import java.util.List;

public class OrderManagementViewModel extends AndroidViewModel {
    private final OrderRepository orderRepository;
    private MutableLiveData<Boolean> isLoading;
    private MutableLiveData<String> errorMessage;
    private MutableLiveData<SearchFilter.FilterState> currentFilter;
    private MutableLiveData<String> filterStatus = new MutableLiveData<>("Tất cả");
    private LiveData<com.example.oss.bean.OrderData> orderDisplays;
    private final MutableLiveData<OrderData> allOrders = new MutableLiveData<>();

    public OrderManagementViewModel(Application application) {
        super(application);
        orderRepository = new OrderRepository(application);
        isLoading = new MutableLiveData<>(false);
        errorMessage = new MutableLiveData<>();
        currentFilter = new MutableLiveData<>(new SearchFilter.FilterState());
        orderDisplays = Transformations.switchMap(filterStatus, status -> {
            return orderRepository.getAllOrderManagementDisplays(mapStatus(status));
        });
    }

    public LiveData<com.example.oss.bean.OrderData> getOrderDisplays() {
        return orderDisplays;
    }

    public LiveData<SearchFilter.FilterState> getCurrentFilter() {
        return currentFilter;
    }

    public void updateFilter(SearchFilter.FilterState filterState) {
        currentFilter.setValue(filterState);
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void clearError() {
        errorMessage.setValue(null);
    }

    public void updateOrderStatus(int orderId, String newStatus) {
        orderRepository.updateOrderStatus(orderId, newStatus);
        String currentStatus = filterStatus.getValue();
        filterStatus.setValue(currentStatus == null ? "Tất cả" : currentStatus);

    }
    public void setFilterStatus(String status) {
        filterStatus.setValue(status);
    }

    private String mapStatus(String vi) {
        switch (vi) {
            case "Chờ xử lý": return "pending";
            case "Hoàn thành": return "delivered";
            case "Đang xử lý": return "confirmed";
            case "Đã giao hàng": return "shipped";
            case "Đã hủy": return "cancelled";
            default: return null; // "Tất cả" hoặc unknown
        }
    }

    public void loadAllOrders() {
        orderRepository.getAllOrderManagementDisplays("Tất cả").observeForever(new Observer<OrderData>() {
            @Override
            public void onChanged(OrderData data) {
                allOrders.postValue(data);
            }
        });
    }

    public LiveData<OrderData> getAllOrders() {
        return allOrders;
    }
}
