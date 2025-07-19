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

        // Intent intent = new Intent(this, UserManagementActivity.class);
        // startActivity(intent);

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

        // Load default fragment based on user role
        if (savedInstanceState == null) {
            loadInitialFragment();
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
            if (isLoggedIn != null && isLoggedIn) {
                // User is logged in, reload initial fragment based on role
                loadInitialFragment();
            }
        }); // Observe current user - chỉ để debug, không dùng để redirect
        authViewModel.getCurrentUser().observe(this, user -> {
            if (user != null) {
                // Update UI with user info - không redirect ở đây để tránh conflict
                // Log thông tin user để debug
                android.util.Log.d("MainActivity",
                        "Current user: " + user.getFullName() + " - Role: " + user.getRole());
            }
        });
    }

    private void updateUIForUser(SessionManager.SessionUser user) {
        // Update bottom navigation or show admin options
        if (user.getRole() == UserRole.ADMIN) {
            // Show admin-specific UI
            // Admin có thể reset sample data nếu cần
            // Có thể thêm menu item admin hoặc chuyển đến AdminFragment
            loadFragment(new AdminFragment());
        } else {
            // Customer - hiển thị HomeFragment
            loadFragment(new HomeFragment());
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }
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
            sampleDataManager.resetSampleData(); // Reset flag first
            sampleDataManager.forceInsertSampleData(); // Then force insert new data
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
    } // Method để load fragment ban đầu dựa trên role

    private void loadInitialFragment() {
        SessionManager sessionManager = SessionManager.getInstance(this);

        // Debug log
        android.util.Log.d("MainActivity", "loadInitialFragment - isLoggedIn: " + sessionManager.isLoggedIn());

        if (sessionManager.isLoggedIn()) {
            SessionManager.SessionUser currentUser = sessionManager.getCurrentUser();
            if (currentUser != null) {
                android.util.Log.d("MainActivity", "Current user: " + currentUser.getFullName() + " - Role: "
                        + currentUser.getRole() + " - Email: " + currentUser.getEmail());

                if (currentUser.getRole() == UserRole.ADMIN) {
                    // Nếu là admin, load AdminFragment
                    android.util.Log.d("MainActivity", "Loading AdminFragment for admin user");
                    loadFragment(new AdminFragment());
                    // Highlight admin option nếu có trong bottom navigation
                    // bottomNavigationView.setSelectedItemId(R.id.nav_admin); // nếu có admin tab
                } else {
                    // Nếu là customer hoặc chưa đăng nhập, load HomeFragment
                    android.util.Log.d("MainActivity", "Loading HomeFragment for customer user");
                    loadFragment(new HomeFragment());
                    bottomNavigationView.setSelectedItemId(R.id.nav_home);
                }
            } else {
                android.util.Log.d("MainActivity", "Current user is null, loading HomeFragment");
                loadFragment(new HomeFragment());
                bottomNavigationView.setSelectedItemId(R.id.nav_home);
            }
        } else {
            // Chưa đăng nhập, load HomeFragment mặc định
            android.util.Log.d("MainActivity", "Not logged in, loading HomeFragment");
            loadFragment(new HomeFragment());
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }
    }
}