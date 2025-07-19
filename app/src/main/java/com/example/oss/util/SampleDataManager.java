package com.example.oss.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.example.oss.database.AppDatabase;
import com.example.oss.entity.*;
import com.example.oss.dao.*;
import java.math.BigDecimal;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SampleDataManager {

    private static final String TAG = "SampleDataManager";
    private static final String PREF_NAME = "SampleDataPrefs";
    private static final String KEY_DATA_INSERTED = "data_inserted";
    private static final String KEY_DATA_VERSION = "data_version";
    private static final int CURRENT_DATA_VERSION = 1;

    private Context context;
    private AppDatabase database;
    private SharedPreferences preferences;
    private ExecutorService executor;

    public SampleDataManager(Context context) {
        this.context = context.getApplicationContext();
        this.database = AppDatabase.getDatabase(context);
        this.preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.executor = Executors.newSingleThreadExecutor();
    }

    /**
     * Check và insert sample data nếu cần
     */
    public void initializeSampleData() {
        executor.execute(() -> {
            try {
                if (shouldInsertData()) {
                    Log.d(TAG, "Bắt đầu insert sample data...");
                    insertSampleData();
                    markDataAsInserted();
                    Log.d(TAG, "Hoàn thành insert sample data!");
                } else {
                    Log.d(TAG, "Sample data đã tồn tại, bỏ qua insert");
                }
            } catch (Exception e) {
                Log.e(TAG, "Lỗi khi insert sample data: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Force insert sample data (để test)
     */
    public void forceInsertSampleData() {
        executor.execute(() -> {
            try {
                Log.d(TAG, "Force insert sample data...");
                clearAllData();
                insertSampleData();
                markDataAsInserted();
                Log.d(TAG, "Hoàn thành force insert sample data!");
            } catch (Exception e) {
                Log.e(TAG, "Lỗi khi force insert sample data: " + e.getMessage(), e);
            }
        });
    }

    private boolean shouldInsertData() {
        boolean dataInserted = preferences.getBoolean(KEY_DATA_INSERTED, false);
        int dataVersion = preferences.getInt(KEY_DATA_VERSION, 0);

        // Check nếu chưa insert hoặc version cũ
        if (!dataInserted || dataVersion < CURRENT_DATA_VERSION) {
            return true;
        }

        // Double check bằng cách kiểm tra database
        try {
            UserDao userDao = database.userDao();
            // Cần thêm method này vào UserDao
            return userDao.getUserCountSync() == 0;
        } catch (Exception e) {
            Log.e(TAG, "Error checking existing data: " + e.getMessage());
            return true; // Insert nếu có lỗi
        }
    }

    private void insertSampleData() {
        insertSampleUsers();
        insertSampleCategories();
        insertSampleProducts();
    }

    private void insertSampleUsers() {
        UserDao userDao = database.userDao();        // 1. Admin user
        User admin = User.builder()
                .email("admin@oss.com")
                .password(SecurityUtils.hashPassword("admin123"))
                .fullName("Admin OSS")
                .phoneNumber("0123456789")
                .role(UserRole.ADMIN.getValue())
                .status(UserStatus.ACTIVE.getValue())
                .build();        // 2. Sample customers
        User customer1 = User.builder()
                .email("customer1@gmail.com")
                .password(SecurityUtils.hashPassword("123456"))
                .fullName("Nguyễn Văn An")
                .phoneNumber("0987654321")
                .role(UserRole.CUSTOMER.getValue())
                .status(UserStatus.ACTIVE.getValue())
                .build();

        User customer2 = User.builder()
                .email("customer2@gmail.com")
                .password(SecurityUtils.hashPassword("123456"))
                .fullName("Trần Thị Bình")
                .phoneNumber("0976543210")
                .role(UserRole.CUSTOMER.getValue())
                .status(UserStatus.ACTIVE.getValue())
                .build();

        User customer3 = User.builder()
                .email("thanhtung")
                .password(SecurityUtils.hashPassword("thanhtung"))
                .fullName("Test User")
                .phoneNumber("0965432109")
                .role(UserRole.CUSTOMER.getValue())
                .status(UserStatus.ACTIVE.getValue())
                .build();

        // Insert users
        userDao.insertUser(admin);
        userDao.insertUser(customer1);
        userDao.insertUser(customer2);
        userDao.insertUser(customer3);

        Log.d(TAG, "Đã insert 4 users (1 admin + 3 customers)");
    }

    private void insertSampleCategories() {
        CategoryDao categoryDao = database.categoryDao();

        Category[] categories = {
                Category.builder()
                        .name("Điện tử")
                        .description("Thiết bị điện tử, công nghệ")
                        .build(),

                Category.builder()
                        .name("Thời trang")
                        .description("Quần áo, phụ kiện thời trang")
                        .build(),

                Category.builder()
                        .name("Sách")
                        .description("Sách văn học, giáo dục, kỹ năng")
                        .build(),

                Category.builder()
                        .name("Thể thao")
                        .description("Dụng cụ thể thao, thể dục")
                        .build(),

                Category.builder()
                        .name("Gia dụng")
                        .description("Đồ gia dụng, nội thất")
                        .build()
        };

        for (Category category : categories) {
            categoryDao.insertCategory(category);
        }

        Log.d(TAG, "Đã insert " + categories.length + " categories");
    }

    private void insertSampleProducts() {
        ProductDao productDao = database.productDao();

        // Điện tử (categoryId = 1) - Sử dụng ảnh thật từ Unsplash
        Product[] electronicsProducts = {
                Product.builder()
                        .categoryId(1)
                        .name("iPhone 15 Pro Max")
                        .description("Smartphone cao cấp với chip A17 Pro, camera 48MP")
                        .price(new BigDecimal("29990000"))
                        .stockQuantity(25)
                        .imageUrl("https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=400&h=400&fit=crop")
                        .isActive(true)
                        .build(),

                Product.builder()
                        .categoryId(1)
                        .name("Samsung Galaxy S24 Ultra")
                        .description("Flagship Android với S Pen, camera zoom 100x")
                        .price(new BigDecimal("31990000"))
                        .stockQuantity(20)
                        .imageUrl("https://images.unsplash.com/photo-1598300042247-d088f8ab3a91?w=400&h=400&fit=crop")
                        .isActive(true)
                        .build(),

                Product.builder()
                        .categoryId(1)
                        .name("MacBook Air M3")
                        .description("Laptop siêu mỏng với chip M3, màn hình Retina 13.6 inch")
                        .price(new BigDecimal("28990000"))
                        .stockQuantity(15)
                        .imageUrl("https://images.unsplash.com/photo-1541807084-5c52b6b3adef?w=400&h=400&fit=crop")
                        .isActive(true)
                        .build(),

                Product.builder()
                        .categoryId(1)
                        .name("AirPods Pro 2")
                        .description("Tai nghe không dây chống ồn chủ động")
                        .price(new BigDecimal("6490000"))
                        .stockQuantity(50)
                        .imageUrl("https://images.unsplash.com/photo-1606220945770-b5b6c2c55bf1?w=400&h=400&fit=crop")
                        .isActive(true)
                        .build()
        };

        // Thời trang (categoryId = 2)
        Product[] fashionProducts = {
                Product.builder()
                        .categoryId(2)
                        .name("Áo sơ mi nam công sở")
                        .description("Áo sơ mi cotton cao cấp, phù hợp môi trường công sở")
                        .price(new BigDecimal("299000"))
                        .stockQuantity(100)
                        .imageUrl("https://images.unsplash.com/photo-1562157873-818bc0726f68?w=400&h=400&fit=crop")
                        .isActive(true)
                        .build(),

                Product.builder()
                        .categoryId(2)
                        .name("Váy đầm nữ dự tiệc")
                        .description("Váy đầm elegant cho các dịp đặc biệt")
                        .price(new BigDecimal("599000"))
                        .stockQuantity(75)
                        .imageUrl("https://images.unsplash.com/photo-1595777457583-95e059d581b8?w=400&h=400&fit=crop")
                        .isActive(true)
                        .build(),

                Product.builder()
                        .categoryId(2)
                        .name("Giày sneaker thể thao")
                        .description("Giày thể thao năng động, phù hợp mọi hoạt động")
                        .price(new BigDecimal("899000"))
                        .stockQuantity(60)
                        .imageUrl("https://images.unsplash.com/photo-1549298916-b41d501d3772?w=400&h=400&fit=crop")
                        .isActive(true)
                        .build()
        };

        // Sách (categoryId = 3)
        Product[] bookProducts = {
                Product.builder()
                        .categoryId(3)
                        .name("Đắc Nhân Tâm")
                        .description("Cuốn sách kinh điển về nghệ thuật ứng xử và giao tiếp")
                        .price(new BigDecimal("69000"))
                        .stockQuantity(200)
                        .imageUrl("https://images.unsplash.com/photo-1544947950-fa07a98d237f?w=400&h=400&fit=crop")
                        .isActive(true)
                        .build(),

                Product.builder()
                        .categoryId(3)
                        .name("Atomic Habits")
                        .description("Hướng dẫn xây dựng thói quen tốt và phá vỡ thói quen xấu")
                        .price(new BigDecimal("129000"))
                        .stockQuantity(150)
                        .imageUrl("https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400&h=400&fit=crop")
                        .isActive(true)
                        .build(),

                Product.builder()
                        .categoryId(3)
                        .name("Lập trình Android với Java")
                        .description("Sách hướng dẫn lập trình Android từ cơ bản đến nâng cao")
                        .price(new BigDecimal("199000"))
                        .stockQuantity(80)
                        .imageUrl("https://images.unsplash.com/photo-1551963831-b3b1ca40c98e?w=400&h=400&fit=crop")
                        .isActive(true)
                        .build()
        };

        // Thể thao (categoryId = 4)
        Product[] sportsProducts = {
                Product.builder()
                        .categoryId(4)
                        .name("Bóng đá FIFA Quality")
                        .description("Bóng đá chính thức FIFA, chất lượng cao")
                        .price(new BigDecimal("349000"))
                        .stockQuantity(40)
                        .imageUrl("https://images.unsplash.com/photo-1551698618-1dfe5d97d256?w=400&h=400&fit=crop")
                        .isActive(true)
                        .build(),

                Product.builder()
                        .categoryId(4)
                        .name("Găng tay boxing")
                        .description("Găng tay boxing chuyên nghiệp, da thật")
                        .price(new BigDecimal("799000"))
                        .stockQuantity(30)
                        .imageUrl("https://images.unsplash.com/photo-1544551763-46a013bb70d5?w=400&h=400&fit=crop")
                        .isActive(true)
                        .build()
        };

        // Gia dụng (categoryId = 5)
        Product[] householdProducts = {
                Product.builder()
                        .categoryId(5)
                        .name("Nồi cơm điện Panasonic")
                        .description("Nồi cơm điện 1.8L, công nghệ fuzzy logic")
                        .price(new BigDecimal("1299000"))
                        .stockQuantity(35)
                        .imageUrl("https://images.unsplash.com/photo-1556909114-f6e7ad7d3136?w=400&h=400&fit=crop")
                        .isActive(true)
                        .build(),

                Product.builder()
                        .categoryId(5)
                        .name("Máy lọc nước RO")
                        .description("Máy lọc nước 9 cấp, công nghệ RO")
                        .price(new BigDecimal("3990000"))
                        .stockQuantity(20)
                        .imageUrl("https://images.unsplash.com/photo-1563453392212-326f5e854473?w=400&h=400&fit=crop")
                        .isActive(true)
                        .build()
        };

        // Insert tất cả products
        for (Product product : electronicsProducts) {
            productDao.insertProduct(product);
        }
        for (Product product : fashionProducts) {
            productDao.insertProduct(product);
        }
        for (Product product : bookProducts) {
            productDao.insertProduct(product);
        }
        for (Product product : sportsProducts) {
            productDao.insertProduct(product);
        }
        for (Product product : householdProducts) {
            productDao.insertProduct(product);
        }

        int totalProducts = electronicsProducts.length + fashionProducts.length +
                bookProducts.length + sportsProducts.length + householdProducts.length;
        Log.d(TAG, "Đã insert " + totalProducts + " products");
    }

    private void clearAllData() {
        // Clear theo thứ tự ngược lại để tránh constraint violation
        database.productDao().deleteAllProducts();
        database.categoryDao().deleteAllCategories();
        database.userDao().deleteAllUsers();
        Log.d(TAG, "Đã xóa toàn bộ data cũ");
    }

    private void markDataAsInserted() {
        preferences.edit()
                .putBoolean(KEY_DATA_INSERTED, true)
                .putInt(KEY_DATA_VERSION, CURRENT_DATA_VERSION)
                .apply();
    }

    /**
     * Reset data (for testing)
     */
    public void resetSampleData() {
        preferences.edit()
                .putBoolean(KEY_DATA_INSERTED, false)
                .putInt(KEY_DATA_VERSION, 0)
                .apply();
        Log.d(TAG, "Đã reset sample data flag");
    }

    /**
     * Check if sample data exists
     */
    public boolean isSampleDataInserted() {
        return preferences.getBoolean(KEY_DATA_INSERTED, false);
    }
}