package com.example.oss.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.example.oss.database.AppDatabase;
import com.example.oss.dao.ProductDao;
import com.example.oss.entity.Product;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProductRepository {
    private ProductDao productDao;
    private LiveData<List<Product>> allProducts;
    private ExecutorService executor;

    public ProductRepository(Application application) {
        AppDatabase database = AppDatabase.getDatabase(application);
        productDao = database.productDao();
        allProducts = productDao.getAllActiveProducts();
        executor = Executors.newFixedThreadPool(2);
    }

    public LiveData<List<Product>> getAllProducts() {
        return allProducts;
    }

    public LiveData<List<Product>> getProductsByCategory(int categoryId) {
        return productDao.getProductsByCategory(categoryId);
    }

    public LiveData<Product> getProductById(int id) {
        return productDao.getProductById(id);
    }

    public LiveData<List<Product>> searchProducts(String query) {
        return productDao.searchProducts(query);
    }

    public void insertProduct(Product product) {
        executor.execute(() -> productDao.insertProduct(product));
    }

    public void updateProduct(Product product) {
        executor.execute(() -> productDao.updateProduct(product));
    }

    public void deleteProduct(Product product) {
        executor.execute(() -> productDao.deleteProduct(product));
    }

    // Insert sample data
    public void insertSampleData() {
        executor.execute(() -> {
            // Sample Products - Electronics (Category ID 1)
            productDao.insertProduct(new Product(1, "iPhone 15 Pro Max",
                    "Điện thoại thông minh cao cấp với chip A17 Pro, camera 48MP và màn hình Super Retina XDR 6.7 inch",
                    new BigDecimal("30990000"), 50,
                    "https://example.com/iphone15.jpg", true));

            productDao.insertProduct(new Product(1, "Samsung Galaxy S24 Ultra",
                    "Smartphone Android flagship với S Pen, camera 200MP và màn hình Dynamic AMOLED 6.8 inch",
                    new BigDecimal("28990000"), 30,
                    "https://example.com/galaxys24.jpg", true));

            productDao.insertProduct(new Product(1, "MacBook Air M3",
                    "Laptop siêu mỏng với chip M3, RAM 16GB, SSD 512GB và màn hình Retina 13.6 inch",
                    new BigDecimal("32990000"), 25,
                    "https://example.com/macbookair.jpg", true));

            productDao.insertProduct(new Product(1, "Dell XPS 13",
                    "Ultrabook Windows cao cấp với Intel Core i7, RAM 16GB và màn hình InfinityEdge",
                    new BigDecimal("25990000"), 20,
                    "https://example.com/dellxps13.jpg", true));

            productDao.insertProduct(new Product(1, "AirPods Pro 2",
                    "Tai nghe true wireless với chống ồn chủ động và chip H2",
                    new BigDecimal("6490000"), 100,
                    "https://example.com/airpods.jpg", true));

            // Fashion (Category ID 2)
            productDao.insertProduct(new Product(2, "Áo Polo Nam UNIQLO",
                    "Áo polo nam chất liệu cotton thoáng mát, nhiều màu sắc",
                    new BigDecimal("590000"), 200,
                    "https://example.com/polo.jpg", true));

            productDao.insertProduct(new Product(2, "Quần Jeans Skinny Fit",
                    "Quần jeans nam ôm vừa phải, chất liệu denim cao cấp",
                    new BigDecimal("890000"), 150,
                    "https://example.com/jeans.jpg", true));

            productDao.insertProduct(new Product(2, "Giày Sneaker Nike Air Force 1",
                    "Giày thể thao classic với thiết kế iconic và đế cao su bền bỉ",
                    new BigDecimal("2990000"), 80,
                    "https://example.com/nike.jpg", true));

            productDao.insertProduct(new Product(2, "Áo Hoodie Unisex",
                    "Áo hoodie unisex chất liệu nỉ cotton ấm áp, phù hợp mọi thời tiết",
                    new BigDecimal("750000"), 120,
                    "https://example.com/hoodie.jpg", true));

            // Books (Category ID 3)
            productDao.insertProduct(new Product(3, "Sapiens: Lược sử loài người",
                    "Cuốn sách bestseller về lịch sử phát triển của loài người từ thời tiền sử đến hiện đại",
                    new BigDecimal("189000"), 300,
                    "https://example.com/sapiens.jpg", true));

            productDao.insertProduct(new Product(3, "Atomic Habits",
                    "Hướng dẫn xây dựng thói quen tốt và loại bỏ thói quen xấu một cách hiệu quả",
                    new BigDecimal("159000"), 250,
                    "https://example.com/atomichabits.jpg", true));

            productDao.insertProduct(new Product(3, "The 7 Habits of Highly Effective People",
                    "Cuốn sách kinh điển về phát triển bản thân và lãnh đạo",
                    new BigDecimal("179000"), 200,
                    "https://example.com/7habits.jpg", true));

            // Home & Garden (Category ID 4)
            productDao.insertProduct(new Product(4, "Robot Hút Bụi Xiaomi",
                    "Robot hút bụi thông minh với navigation laser và app điều khiển",
                    new BigDecimal("4990000"), 50,
                    "https://example.com/robot.jpg", true));

            productDao.insertProduct(new Product(4, "Nồi Cơm Điện Panasonic 1.8L",
                    "Nồi cơm điện cao cấp với lòng nồi phủ kim cương và nhiều chế độ nấu",
                    new BigDecimal("2590000"), 60,
                    "https://example.com/ricecooker.jpg", true));

            productDao.insertProduct(new Product(4, "Chậu Cây Cảnh Trang Trí",
                    "Chậu cây bằng gốm sứ cao cấp, thiết kế hiện đại phù hợp mọi không gian",
                    new BigDecimal("299000"), 150,
                    "https://example.com/pot.jpg", true));
        });
    }
}