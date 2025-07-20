# OSS Store - Setup Guide

## Hướng dẫn thiết lập dự án

### 1. Clone dự án

```bash
git clone <repository-url>
cd OSS
```

### 2. Thiết lập API Keys (Quan trọng!)

#### Cách 1: Sử dụng local.properties (Khuyến nghị)

1. Copy file `local.properties.template` thành `local.properties`:

```bash
cp local.properties.template local.properties
```

2. Mở file `local.properties` và thay thế các giá trị:

```properties
# SDK Location (Android Studio sẽ tự động thêm)
sdk.dir=YOUR_ANDROID_SDK_PATH

# Mailgun API Key
MAILGUN_API_KEY=your_actual_mailgun_api_key_here
```

#### Cách 2: Sử dụng biến môi trường hệ thống

```bash
# Windows
set MAILGUN_API_KEY=your_api_key_here

# Linux/Mac
export MAILGUN_API_KEY=your_api_key_here
```

### 3. Lấy Mailgun API Key

1. Đăng ký tài khoản tại: https://app.mailgun.com/
2. Vào **Account Security** → **API Keys**
3. Copy **Private API key**

### 4. Build dự án

```bash
./gradlew build
```

### 5. Chạy ứng dụng

- Mở project trong Android Studio
- Chạy trên emulator hoặc thiết bị thật

## Tính năng quên mật khẩu

### Cách hoạt động:

1. Người dùng nhập email vào màn hình "Quên mật khẩu"
2. Hệ thống sinh mật khẩu mới ngẫu nhiên
3. Gửi email chứa mật khẩu mới qua Mailgun template
4. Người dùng dùng mật khẩu mới để đăng nhập

### Template Mailgun:

- Tên template: `Forget password`
- Variables: `fullname`, `password`

## Bảo mật

- ❌ Không bao giờ commit API keys vào git
- ✅ Sử dụng `local.properties` (đã được gitignore)
- ✅ Hoặc sử dụng biến môi trường
- ✅ File `local.properties.template` để hướng dẫn team

## Troubleshooting

### Lỗi "MAILGUN_API_KEY not found"

- Kiểm tra file `local.properties` có tồn tại không
- Kiểm tra API key có đúng format không
- Rebuild project: `./gradlew clean build`

### Lỗi SSL/HTTP (NoSuchFieldError)

- ✅ **Đã sửa**: Thay Unirest bằng OkHttp
- Nếu vẫn lỗi: Clean project và rebuild

### Lỗi gửi email

1. **Kiểm tra logs trong Android Studio:**

   ```
   I/MailGun: Email sent successfully to: user@example.com
   E/MailGun: Failed to send email: 401 - Unauthorized
   E/MailGun: Error sending email to: user@example.com
   ```

2. **Các lỗi phổ biến:**

   - **401 Unauthorized**: API key sai hoặc hết hạn
   - **400 Bad Request**: Template "Forget password" không tồn tại
   - **403 Forbidden**: Domain chưa verify
   - **Network error**: Kiểm tra kết nối internet

3. **Test kết nối Mailgun:**
   ```java
   // Thêm vào code test
   boolean result = MailGun.testMailgunConnection();
   Log.d("Test", "Mailgun test: " + result);
   ```

### Kiểm tra template Mailgun

1. Đăng nhập https://app.mailgun.com/
2. Vào **Sending** → **Templates**
3. Đảm bảo có template tên: `Forget password`
4. Template phải có variables: `{{fullname}}` và `{{password}}`
