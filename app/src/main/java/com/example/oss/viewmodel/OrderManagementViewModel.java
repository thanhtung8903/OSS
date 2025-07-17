package com.example.oss.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.oss.repository.OrderRepository;
import com.example.oss.util.SearchFilter;

import java.util.List;

public class OrderManagementViewModel extends AndroidViewModel {
    private OrderRepository orderRepository;
    private MutableLiveData<Boolean> isLoading;
    private MutableLiveData<String> errorMessage;
    private MutableLiveData<SearchFilter.FilterState> currentFilter;
    private final LiveData<com.example.oss.bean.OrderData> orderDisplays;

    public OrderManagementViewModel(Application application){
        super(application);
        orderRepository = new OrderRepository(application);
        isLoading = new MutableLiveData<>(false);
        errorMessage = new MutableLiveData<>();
        currentFilter = new MutableLiveData<>(new SearchFilter.FilterState());
        orderDisplays = orderRepository.getAllOrderManagementDisplays();
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
}
