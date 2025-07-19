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
import com.example.oss.dao.AddressDao;
import com.example.oss.dao.OrderDao;
import com.example.oss.dao.OrderItemDao;
import com.example.oss.dao.ReviewDao;
import com.example.oss.entity.Address;
import com.example.oss.entity.Order;
import com.example.oss.entity.OrderItem;
import com.example.oss.entity.Review;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

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

        public void forceInsertSampleData() {
                executor.execute(() -> {
                        try {
                                Log.d(TAG, "Force insert sample data...");
                                clearAllData();
                                insertSampleUsers();
                                insertSampleCategories();
                                List<Long> productIds = insertSampleProducts();
                                insertSampleAddresses();
                                insertSampleOrders();
                                insertSampleReviews(productIds);
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

                try {
                        UserDao userDao = database.userDao();
                        // Cần thêm method này vào UserDao
                        return userDao.getUserCountSync() == 0;
                } catch (Exception e) {
                        Log.e(TAG, "Error checking existing data: " + e.getMessage());
                        return true;
                }
        }

        private void insertSampleData() {
                insertSampleUsers();
                insertSampleCategories();
                List<Long> productIds = insertSampleProducts(); // Lưu lại product IDs
                insertSampleAddresses();
                insertSampleOrders();
                insertSampleReviews(productIds); // Truyền product IDs vào
        }

        private void insertSampleUsers() {
                UserDao userDao = database.userDao(); //
                User admin = User.builder()
                                .email("admin@oss.com")
                                .password(SecurityUtils.hashPassword("admin123"))
                                .fullName("Admin OSS")
                                .phoneNumber("0123456789")
                                .role(UserRole.ADMIN.getValue())
                                .status(UserStatus.ACTIVE.getValue())
                                .build(); // 2. Sample customers
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

                User customer4 = User.builder()
                                .email("thuyduong")
                                .password(SecurityUtils.hashPassword("thuyduong"))
                                .fullName("Test User")
                                .phoneNumber("0965432101")
                                .role(UserRole.CUSTOMER.getValue())
                                .status(UserStatus.ACTIVE.getValue())
                                .build();

                userDao.insertUser(admin);
                userDao.insertUser(customer1);
                userDao.insertUser(customer2);
                userDao.insertUser(customer3);
                userDao.insertUser(customer4);

                Log.d(TAG, "Đã insert 5 users (1 admin + 4 customers)");
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

        private List<Long> insertSampleProducts() {
                ProductDao productDao = database.productDao();
                List<Long> productIds = new ArrayList<>();

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
                                                .imageUrl("https://images.unsplash.com/photo-1525825691042-e14d9042fc70??w=400&h=400&fit=crop")
                                                .isActive(true)
                                                .build()
                };

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

                // Insert products and collect their IDs
                for (Product product : electronicsProducts) {
                        long id = productDao.insertProduct(product);
                        productIds.add(id);
                }
                for (Product product : fashionProducts) {
                        long id = productDao.insertProduct(product);
                        productIds.add(id);
                }
                for (Product product : bookProducts) {
                        long id = productDao.insertProduct(product);
                        productIds.add(id);
                }
                for (Product product : sportsProducts) {
                        long id = productDao.insertProduct(product);
                        productIds.add(id);
                }
                for (Product product : householdProducts) {
                        long id = productDao.insertProduct(product);
                        productIds.add(id);
                }

                int totalProducts = electronicsProducts.length + fashionProducts.length +
                                bookProducts.length + sportsProducts.length + householdProducts.length;
                Log.d(TAG, "Đã insert " + totalProducts + " products với IDs: " + productIds.toString());

                return productIds;
        }

        private void insertSampleAddresses() {
                AddressDao addressDao = database.addressDao();

                try {
                        // Địa chỉ cho User 1 (John Doe)
                        Address address1 = new Address(1, "John Doe", "0901234567",
                                        "123 Nguyễn Huệ", "Quận 1", "TP. Hồ Chí Minh", "70000", true, "HOME",
                                        "Gần chợ Bến Thành");
                        addressDao.insertAddress(address1);

                        Address address1_2 = new Address(1, "John Doe", "0901234567",
                                        "456 Lê Lợi", "Quận 3", "TP. Hồ Chí Minh", "70000", false, "OFFICE",
                                        "Văn phòng công ty");
                        addressDao.insertAddress(address1_2);

                        // Địa chỉ cho User 2 (Jane Smith)
                        Address address2 = new Address(2, "Jane Smith", "0987654321",
                                        "789 Trần Hưng Đạo", "Quận 5", "TP. Hồ Chí Minh", "70000", true, "HOME",
                                        "Gần chợ An Đông");
                        addressDao.insertAddress(address2);

                        // Địa chỉ cho User 3 (Mike Johnson)
                        Address address3 = new Address(3, "Mike Johnson", "0912345678",
                                        "321 Điện Biên Phủ", "Quận Bình Thạnh", "TP. Hồ Chí Minh", "70000", true,
                                        "HOME", "");
                        addressDao.insertAddress(address3);

                        // Địa chỉ cho User 4 (Sarah Wilson)
                        Address address4 = new Address(4, "Sarah Wilson", "0923456789",
                                        "654 Võ Văn Tần", "Quận 3", "TP. Hồ Chí Minh", "70000", true, "HOME",
                                        "Gần công viên Tao Đàn");
                        addressDao.insertAddress(address4);

                        Log.d("SampleDataManager", "Sample addresses inserted successfully");
                } catch (Exception e) {
                        Log.e("SampleDataManager", "Error inserting sample addresses", e);
                }
        }

        private void insertSampleOrders() {
                OrderDao orderDao = database.orderDao();
                OrderItemDao orderItemDao = database.orderItemDao();

                try {
                        // Order 1 - User 1 đã mua và đã nhận hàng
                        Order order1 = Order.builder()
                                        .userId(1)
                                        .shippingAddressId(1)
                                        .totalAmount(new BigDecimal("2999000"))
                                        .status("delivered")
                                        .orderDate(new Date(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000)) // 7
                                                                                                                   // ngày
                                                                                                                   // trước
                                        .paymentMethod("card")
                                        .build();
                        long orderId1 = orderDao.insertOrder(order1);

                        // Order items cho order 1
                        OrderItem item1_1 = new OrderItem((int) orderId1, 1, 1, new BigDecimal("1499000")); // iPhone 13
                        OrderItem item1_2 = new OrderItem((int) orderId1, 3, 1, new BigDecimal("1500000")); // Samsung
                                                                                                            // Galaxy
                                                                                                            // S21
                        orderItemDao.insertOrderItem(item1_1);
                        orderItemDao.insertOrderItem(item1_2);

                        // Order 2 - User 2 đã mua và đã nhận hàng
                        Order order2 = Order.builder()
                                        .userId(2)
                                        .shippingAddressId(3)
                                        .totalAmount(new BigDecimal("899000"))
                                        .status("delivered")
                                        .orderDate(new Date(System.currentTimeMillis() - 5 * 24 * 60 * 60 * 1000)) // 5
                                                                                                                   // ngày
                                                                                                                   // trước
                                        .paymentMethod("cash")
                                        .build();
                        long orderId2 = orderDao.insertOrder(order2);

                        // Order items cho order 2
                        OrderItem item2_1 = new OrderItem((int) orderId2, 5, 1, new BigDecimal("899000")); // Nike Air
                                                                                                           // Max
                        orderItemDao.insertOrderItem(item2_1);

                        // Order 3 - User 3 đã mua và đã nhận hàng
                        Order order3 = Order.builder()
                                        .userId(3)
                                        .shippingAddressId(5)
                                        .totalAmount(new BigDecimal("1899000"))
                                        .status("delivered")
                                        .orderDate(new Date(System.currentTimeMillis() - 3 * 24 * 60 * 60 * 1000)) // 3
                                                                                                                   // ngày
                                                                                                                   // trước
                                        .paymentMethod("transfer")
                                        .build();
                        long orderId3 = orderDao.insertOrder(order3);

                        // Order items cho order 3
                        OrderItem item3_1 = new OrderItem((int) orderId3, 2, 1, new BigDecimal("999000")); // MacBook
                                                                                                           // Pro
                        OrderItem item3_2 = new OrderItem((int) orderId3, 4, 1, new BigDecimal("799000")); // Áo khoác
                                                                                                           // denim
                        OrderItem item3_3 = new OrderItem((int) orderId3, 6, 1, new BigDecimal("101000")); // Protein
                                                                                                           // Whey
                        orderItemDao.insertOrderItem(item3_1);
                        orderItemDao.insertOrderItem(item3_2);
                        orderItemDao.insertOrderItem(item3_3);

                        // Order 4 - User 4 đơn hàng mới
                        Order order4 = Order.builder()
                                        .userId(4)
                                        .shippingAddressId(7)
                                        .totalAmount(new BigDecimal("1250000"))
                                        .status("confirmed")
                                        .orderDate(new Date(System.currentTimeMillis() - 1 * 24 * 60 * 60 * 1000)) // 1
                                                                                                                   // ngày
                                                                                                                   // trước
                                        .paymentMethod("card")
                                        .build();
                        long orderId4 = orderDao.insertOrder(order4);

                        // Order items cho order 4
                        OrderItem item4_1 = new OrderItem((int) orderId4, 7, 1, new BigDecimal("1250000")); // Găng tay
                                                                                                            // boxing
                        orderItemDao.insertOrderItem(item4_1);

                        Log.d("SampleDataManager", "Sample orders inserted successfully");
                } catch (Exception e) {
                        Log.e("SampleDataManager", "Error inserting sample orders", e);
                }
        }

        private void insertSampleReviews(List<Long> productIds) {
                ReviewDao reviewDao = database.reviewDao();

                try {
                        // Kiểm tra xem có đủ product IDs không
                        if (productIds.size() < 4) {
                                Log.e("SampleDataManager",
                                                "Không đủ product IDs để tạo reviews. Có: " + productIds.size());
                                return;
                        }

                        // Reviews từ User 2 cho các sản phẩm đầu tiên
                        Review review1 = new Review(2, productIds.get(0).intValue(), 5,
                                        "iPhone 15 Pro Max rất tuyệt vời! Pin trâu, camera chụp đẹp, hiệu năng mượt mà. Đáng đồng tiền bát gạo!");
                        review1.setCreatedAt(new Date(System.currentTimeMillis() - 6 * 24 * 60 * 60 * 1000));
                        reviewDao.insertReview(review1);

                        Review review2 = new Review(2, productIds.get(1).intValue(), 4,
                                        "Samsung Galaxy S24 Ultra ổn, màn hình đẹp, S Pen tiện dụng. Tổng thể rất hài lòng cho tầm giá.");
                        review2.setCreatedAt(new Date(System.currentTimeMillis() - 6 * 24 * 60 * 60 * 1000));
                        reviewDao.insertReview(review2);

                        // Reviews từ User 3
                        Review review3 = new Review(3, productIds.get(2).intValue(), 5,
                                        "MacBook Air M3 quá đỉnh! Hiệu năng khủng, pin siêu trâu, thiết kế sang trọng. Highly recommended!");
                        review3.setCreatedAt(new Date(System.currentTimeMillis() - 4 * 24 * 60 * 60 * 1000));
                        reviewDao.insertReview(review3);

                        Review review4 = new Review(3, productIds.get(3).intValue(), 4,
                                        "AirPods Pro 2 chất lượng âm thanh tuyệt vời, chống ồn hiệu quả. Pin tốt, đáng mua.");
                        review4.setCreatedAt(new Date(System.currentTimeMillis() - 4 * 24 * 60 * 60 * 1000));
                        reviewDao.insertReview(review4);

                        // Reviews từ User 4
                        if (productIds.size() > 4) {
                                Review review5 = new Review(4, productIds.get(4).intValue(), 5,
                                                "Áo sơ mi nam chất lượng cao, form đẹp, vải mềm mại. Phù hợp cho môi trường công sở.");
                                review5.setCreatedAt(new Date(System.currentTimeMillis() - 3 * 24 * 60 * 60 * 1000));
                                reviewDao.insertReview(review5);
                        }

                        if (productIds.size() > 5) {
                                Review review6 = new Review(4, productIds.get(5).intValue(), 4,
                                                "Váy đầm rất đẹp, chất liệu tốt, form dáng thanh lịch. Thích hợp cho các buổi tiệc.");
                                review6.setCreatedAt(new Date(System.currentTimeMillis() - 3 * 24 * 60 * 60 * 1000));
                                reviewDao.insertReview(review6);
                        }

                        // Thêm vài reviews nữa cho sản phẩm được nhiều người mua
                        Review review7 = new Review(5, productIds.get(0).intValue(), 4,
                                        "iPhone 15 Pro Max camera tốt, màn hình sáng rõ. Tổng thể vẫn recommend cho ai muốn upgrade.");
                        review7.setCreatedAt(new Date(System.currentTimeMillis() - 2 * 24 * 60 * 60 * 1000));
                        reviewDao.insertReview(review7);

                        if (productIds.size() > 6) {
                                Review review8 = new Review(5, productIds.get(6).intValue(), 5,
                                                "Giày sneaker siêu thoải mái! Chạy bộ, đi bộ đều ok. Thiết kế trendy, đi với outfit nào cũng đẹp.");
                                review8.setCreatedAt(new Date(System.currentTimeMillis() - 1 * 24 * 60 * 60 * 1000));
                                reviewDao.insertReview(review8);
                        }

                        Log.d("SampleDataManager",
                                        "Sample reviews inserted successfully với " + productIds.size() + " products");
                } catch (Exception e) {
                        Log.e("SampleDataManager", "Error inserting sample reviews", e);
                }
        }

        private void clearAllData() {
                // Clear all tables in correct order (foreign key dependencies)
                database.reviewDao().deleteAll();
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

        public void resetSampleData() {
                preferences.edit()
                                .putBoolean(KEY_DATA_INSERTED, false)
                                .putInt(KEY_DATA_VERSION, 0)
                                .apply();
                Log.d(TAG, "Đã reset sample data flag");
        }

        public boolean isSampleDataInserted() {
                return preferences.getBoolean(KEY_DATA_INSERTED, false);
        }

        // Method để test - force recreate all sample data
        public void recreateSampleData() {
                executor.execute(() -> {
                        try {
                                Log.d(TAG, "Recreating sample data...");

                                // Reset preferences để force recreate
                                resetSampleData();

                                // Clear all data
                                clearAllData();

                                // Insert fresh data
                                insertSampleUsers();
                                insertSampleCategories();
                                List<Long> productIds = insertSampleProducts();
                                insertSampleAddresses();
                                insertSampleOrders();
                                insertSampleReviews(productIds);

                                // Mark as completed
                                markDataAsInserted();

                                Log.d(TAG, "Sample data recreated successfully!");
                        } catch (Exception e) {
                                Log.e(TAG, "Error recreating sample data: " + e.getMessage(), e);
                        }
                });
        }
}