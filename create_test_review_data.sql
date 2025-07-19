-- Script tạo dữ liệu test cho Review functionality
-- Chạy script này trong SQLite để tạo data test

-- 1. Tạo order delivered cho user_id = 1
INSERT OR REPLACE INTO orders (id, user_id, total_amount, status, created_at, address_id) 
VALUES (100, 1, 899000, 'delivered', datetime('now', '-7 days'), 1);

-- 2. Tạo order_item cho sản phẩm id = 1 (Giày sneaker thể thao)
INSERT OR REPLACE INTO order_items (id, order_id, product_id, quantity, price) 
VALUES (100, 100, 1, 1, 899000);

-- 3. Tạo order_item cho sản phẩm id = 2 (nếu có sản phẩm khác)
INSERT OR REPLACE INTO order_items (id, order_id, product_id, quantity, price) 
VALUES (101, 100, 2, 1, 0);

-- 4. Xóa review cũ nếu có (để test lại từ đầu)
DELETE FROM reviews WHERE user_id = 1 AND product_id IN (1, 2);

-- Kiểm tra kết quả:
-- SELECT * FROM orders WHERE id = 100;
-- SELECT * FROM order_items WHERE order_id = 100;
-- SELECT * FROM reviews WHERE user_id = 1; 