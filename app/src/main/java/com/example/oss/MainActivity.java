package com.example.oss;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import android.view.View;

import com.example.oss.activity.CategoryManagementActivity;
import com.example.oss.activity.UserManagementActivity;
import com.example.oss.fragment.AdminFragment;
import com.example.oss.ui.auth.RegisterActivity;
import com.example.oss.viewmodel.AuthViewModel;
import com.example.oss.fragment.HomeFragment;
import com.example.oss.fragment.SearchFragment;
import com.example.oss.fragment.CartFragment;
import com.example.oss.fragment.WishlistFragment;
import com.example.oss.fragment.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.oss.util.SessionManager;
import com.example.oss.util.UserRole;
import com.example.oss.util.SampleDataManager;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private AuthViewModel authViewModel;
    private SampleDataManager sampleDataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        Intent intent = new Intent(this, UserManagementActivity.class);
//        startActivity(intent);
        // Initialize SampleDataManager và load sample data
        initializeSampleData();
        // Initialize ViewModel
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Check session first
        authViewModel.checkSession();

        // Initialize Views
        initViews();
        setupBottomNavigation();
        setupObservers();

        // Load default fragment nếu không phải admin
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }
    }

    private void initViews() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.nav_search) {
                selectedFragment = new SearchFragment();
            } else if (itemId == R.id.nav_cart) {
                selectedFragment = new CartFragment();
            } else if (itemId == R.id.nav_wishlist) {
                selectedFragment = new WishlistFragment();
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            } else if (itemId == R.id.nav_admin) {
                selectedFragment = new AdminFragment();
            }
            if (selectedFragment != null) {
                loadFragment(selectedFragment);
                return true;
            }
            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    private void setupObservers() {
        // Observe authentication state
        authViewModel.getIsLoggedIn().observe(this, isLoggedIn -> {
            // Update UI based on login state
            // Can show/hide certain menu items or features
        });

        // Observe current user
        authViewModel.getCurrentUser().observe(this, user -> {
            if (user != null) {
                // Update UI with user info
                // Show admin features if user is admin
                updateUIForUser(user);
            }
        });
    }

    private void updateUIForUser(SessionManager.SessionUser user) {
        // Không còn logic menu admin, chỉ dùng menu user
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Đảm bảo tab admin được cập nhật đúng khi quay lại activity
        SessionManager.SessionUser user = SessionManager.getInstance(this).getCurrentUser();
        if (user != null) {
            updateUIForUser(user);
        }
    }

    /**
     * Method để force reset sample data (dành cho admin hoặc testing)
     */
    public void resetSampleData() {
        if (sampleDataManager != null) {
            sampleDataManager.forceInsertSampleData();
        }
    }

    // Method để check login trước khi thực hiện action cần authentication
    public boolean checkLoginRequired() {
        Boolean isLoggedIn = authViewModel.getIsLoggedIn().getValue();
        return isLoggedIn != null && isLoggedIn;
    }

    // Method để redirect về login khi cần
    public void requireLogin() {
        // Sẽ implement sau khi tạo các fragment
    }

    /**
     * Initialize sample data khi app khởi chạy
     */
    private void initializeSampleData() {
        sampleDataManager = new SampleDataManager(this);
        // Chạy async để không block UI
        sampleDataManager.initializeSampleData();
    }
}