package com.example.oss.fragment;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.oss.MainActivity;
import com.example.oss.ui.auth.LoginActivity;
import com.example.oss.viewmodel.AuthViewModel;
import com.example.oss.util.SessionManager;

public abstract class BaseFragment extends Fragment {

    protected AuthViewModel authViewModel;
    protected SessionManager sessionManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize AuthViewModel early in lifecycle to prevent NPE
        initAuthViewModel();
        sessionManager = SessionManager.getInstance(requireContext());
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

    // Get current user
    protected SessionManager.SessionUser getCurrentUser() {
        return sessionManager.getCurrentUser();
    }

    // Get current user ID (helper method)
    protected int getCurrentUserId() {
        return sessionManager.getCurrentUserId();
    }

    // Check if current user has specific role
    protected boolean hasRole(com.example.oss.util.UserRole role) {
        return sessionManager.hasRole(role);
    }

    // Check if current user is admin
    protected boolean isAdmin() {
        return sessionManager.isAdmin();
    }

    // Check if current user is customer
    protected boolean isCustomer() {
        return sessionManager.isCustomer();
    }

    // Update session (extend timeout)
    protected void updateSession() {
        sessionManager.updateSession();
    }

    // Logout current user
    protected void logout() {
        sessionManager.logoutUser();
        // Optionally redirect to login screen
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }
}