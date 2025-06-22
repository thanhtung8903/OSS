package com.example.oss.fragment;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.oss.MainActivity;
import com.example.oss.ui.auth.LoginActivity;
import com.example.oss.viewmodel.AuthViewModel;

public abstract class BaseFragment extends Fragment {

    protected AuthViewModel authViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize AuthViewModel early in lifecycle to prevent NPE
        initAuthViewModel();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Ensure AuthViewModel is available
        if (authViewModel == null) {
            initAuthViewModel();
        }
    }

    private void initAuthViewModel() {
        try {
            authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
        } catch (Exception e) {
            // Fallback if activity is not ready yet
            authViewModel = null;
        }
    }

    // Method để check login và redirect nếu cần
    protected boolean requireLogin() {
        if (authViewModel == null) {
            initAuthViewModel();
        }

        if (authViewModel == null) {
            // If still null, redirect to login as safety measure
            redirectToLogin();
            return false;
        }

        Boolean isLoggedIn = authViewModel.getIsLoggedIn().getValue();
        if (isLoggedIn == null || !isLoggedIn) {
            redirectToLogin();
            return false;
        }
        return true;
    }

    // Method để check login mà không redirect
    protected boolean isLoggedIn() {
        if (authViewModel == null) {
            initAuthViewModel();
        }

        if (authViewModel == null) {
            return false; // Safe fallback
        }

        Boolean loggedIn = authViewModel.getIsLoggedIn().getValue();
        return loggedIn != null && loggedIn;
    }

    private void redirectToLogin() {
        Intent intent = new Intent(getContext(), LoginActivity.class);
        startActivity(intent);
    }
}