package com.example.oss.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import com.example.oss.MainActivity;
import com.example.oss.R;
import com.example.oss.entity.User;
import com.example.oss.ui.auth.LoginActivity;
import com.example.oss.util.SessionManager;
import com.example.oss.viewmodel.AuthViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.example.oss.ui.profile.EditProfileActivity;
import com.example.oss.ui.profile.ChangePasswordActivity;
import android.app.Activity;
import android.app.AlertDialog;
import com.example.oss.util.UserRole;

public class ProfileFragment extends BaseFragment {

    private View layoutNotLoggedIn;
    private View layoutLoggedIn;
    private MaterialButton btnLogin;
    private MaterialButton btnLogout;
    private TextView tvWelcome, tvUserName, tvUserEmail, tvUserPhone;
    private MaterialCardView cardPersonalInfo, cardOrders, cardAddresses, cardChangePassword, cardSettings;
    private static final int EDIT_PROFILE_REQUEST = 1001;
    private static final int CHANGE_PASSWORD_REQUEST = 1002;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupListeners();

        // Check login status and show appropriate UI
        checkLoginAndLoadData();

        // Observe authentication state changes
        observeAuthState();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Re-check login status when returning to fragment
        checkLoginAndLoadData();
    }

//    @Override
//    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
//        // Nếu user là admin, show thêm menu reset data
//        SessionManager.SessionUser currentUser = getCurrentUser();
//        if (currentUser != null && currentUser.getRole() == UserRole.ADMIN) {
//            menu.add(0, R.id.menu_reset_data, 0, "Reset Sample Data")
//                    .setIcon(R.drawable.ic_settings)
//                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
//        }
//        super.onCreateOptionsMenu(menu, inflater);
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        if (item.getItemId() == R.id.menu_reset_data) {
//            // Confirm dialog trước khi reset
//            new AlertDialog.Builder(requireContext())
//                    .setTitle("Reset Sample Data")
//                    .setMessage("Xóa toàn bộ data và insert lại sample data?")
//                    .setPositiveButton("Reset", (dialog, which) -> {
//                        if (getActivity() instanceof MainActivity) {
//                            ((MainActivity) getActivity()).resetSampleData();
//                            Toast.makeText(getContext(), "Đã reset sample data!", Toast.LENGTH_SHORT).show();
//                        }
//                    })
//                    .setNegativeButton("Hủy", null)
//                    .show();
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }

    private void initViews(View view) {
        layoutNotLoggedIn = view.findViewById(R.id.layout_not_logged_in);
        layoutLoggedIn = view.findViewById(R.id.layout_logged_in);
        btnLogin = view.findViewById(R.id.btn_login);
        btnLogout = view.findViewById(R.id.btn_logout);
        tvWelcome = view.findViewById(R.id.tv_welcome);
        tvUserName = view.findViewById(R.id.tv_user_name);
        tvUserEmail = view.findViewById(R.id.tv_user_email);
        tvUserPhone = view.findViewById(R.id.tv_user_phone);
        cardPersonalInfo = view.findViewById(R.id.card_personal_info);
        cardOrders = view.findViewById(R.id.card_orders);
        cardAddresses = view.findViewById(R.id.card_addresses);
        cardChangePassword = view.findViewById(R.id.card_change_password);
        cardSettings = view.findViewById(R.id.card_settings);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), LoginActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            if (authViewModel != null) {
                authViewModel.logout();
                Toast.makeText(getContext(), "Đã đăng xuất", Toast.LENGTH_SHORT).show();
            }
        });

        cardPersonalInfo.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), EditProfileActivity.class);
            startActivityForResult(intent, EDIT_PROFILE_REQUEST);
        });

        cardOrders.setOnClickListener(v -> {
            // TODO: Navigate to order history
            Toast.makeText(getContext(), "Lịch sử đơn hàng", Toast.LENGTH_SHORT).show();
        });

        cardAddresses.setOnClickListener(v -> {
            // TODO: Navigate to address management
            Toast.makeText(getContext(), "Quản lý địa chỉ", Toast.LENGTH_SHORT).show();
        });

        cardChangePassword.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ChangePasswordActivity.class);
            startActivityForResult(intent, CHANGE_PASSWORD_REQUEST);
        });

        cardSettings.setOnClickListener(v -> {
            // TODO: Navigate to settings
            Toast.makeText(getContext(), "Cài đặt", Toast.LENGTH_SHORT).show();
        });
    }

    private void observeAuthState() {
        if (authViewModel != null) {
            // Observe login state changes
            authViewModel.getIsLoggedIn().observe(getViewLifecycleOwner(), isLoggedIn -> {
                if (isLoggedIn != null) {
                    if (isLoggedIn) {
                        showLoggedInUI();
                        loadUserData();
                    } else {
                        showNotLoggedInUI();
                    }
                }
            });

            // Observe current user data
            authViewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
                if (user != null) {
                    displayUserInfo(user);
                }
            });
        }
    }

    private void checkLoginAndLoadData() {
        if (isLoggedIn()) {
            showLoggedInUI();
            loadUserData();
        } else {
            showNotLoggedInUI();
        }
    }

    private void loadUserData() {
        // Load data from SessionManager for immediate display
        SessionManager sessionManager = SessionManager.getInstance(getContext());
        SessionManager.SessionUser sessionUser = sessionManager.getLoggedInUser();
        if (sessionUser != null) {
            displayUserInfo(sessionUser);
        }

        // Data will also be loaded automatically through observers
        // authViewModel.getCurrentUser() is already observed
    }

    private void displayUserInfo(User user) {
        if (user != null) {
            tvWelcome.setText("Xin chào, " + user.getFullName() + "!");
            tvUserName.setText(user.getFullName());
            tvUserEmail.setText(user.getEmail());
            tvUserPhone.setText(user.getPhoneNumber());
        }
    }

    private void displayUserInfo(SessionManager.SessionUser sessionUser) {
        if (sessionUser != null) {
            tvWelcome.setText("Xin chào, " + sessionUser.getFullName() + "!");
            tvUserName.setText(sessionUser.getFullName());
            tvUserEmail.setText(sessionUser.getEmail());
            String phone = sessionUser.getPhoneNumber();
            tvUserPhone.setText(phone != null && !phone.isEmpty() ? phone : "Chưa cập nhật");
        }
    }

    private void showNotLoggedInUI() {
        layoutNotLoggedIn.setVisibility(View.VISIBLE);
        layoutLoggedIn.setVisibility(View.GONE);
    }

    private void showLoggedInUI() {
        layoutNotLoggedIn.setVisibility(View.GONE);
        layoutLoggedIn.setVisibility(View.VISIBLE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == EDIT_PROFILE_REQUEST && resultCode == Activity.RESULT_OK) {
            // Refresh user data sau khi edit
            loadUserData();
            Toast.makeText(getContext(), "Thông tin đã được cập nhật", Toast.LENGTH_SHORT).show();
        } else if (requestCode == CHANGE_PASSWORD_REQUEST && resultCode == Activity.RESULT_OK) {
            Toast.makeText(getContext(), "Mật khẩu đã được thay đổi thành công", Toast.LENGTH_SHORT).show();
        }
    }
}