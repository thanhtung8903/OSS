package com.example.oss.util;

import com.example.oss.BuildConfig;
import okhttp3.*;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class MailGun {
    private static final String DOMAIN = "mg.thanhtung.me";
    private static final String FROM_EMAIL = "Mailgun Sandbox <postmaster@mg.thanhtung.me>";

    /**
     * Test method để kiểm tra kết nối Mailgun
     */
    public static boolean testMailgunConnection() {
        return sendResetPasswordEmail("test@example.com", "Test User", "TestPass123");
    }

    /**
     * Gửi email reset password sử dụng OkHttp
     */
    public static boolean sendResetPasswordEmail(String userEmail, String userName, String newPassword) {
        try {
            String apiKey = BuildConfig.MAILGUN_API_KEY;

            // Tạo OkHttp client với timeout
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();

            // Tạo JSON variables cho template
            String templateVariables = String.format(
                    "{\"fullname\": \"%s\", \"password\": \"%s\"}",
                    userName, newPassword);

            // Tạo credentials cho Basic Auth
            String credentials = Credentials.basic("api", apiKey);

            // Tạo form data
            RequestBody requestBody = new FormBody.Builder()
                    .add("from", FROM_EMAIL)
                    .add("to", userEmail)
                    .add("subject", "Mật khẩu mới - OSS App")
                    .add("template", "Forget password")
                    .add("h:X-Mailgun-Variables", templateVariables)
                    .build();

            // Tạo request
            Request request = new Request.Builder()
                    .url("https://api.mailgun.net/v3/" + DOMAIN + "/messages")
                    .header("Authorization", credentials)
                    .post(requestBody)
                    .build();

            // Gửi request
            try (Response response = client.newCall(request).execute()) {
                boolean success = response.isSuccessful();
                if (success) {
                    android.util.Log.i("MailGun", "Email sent successfully to: " + userEmail);
                } else {
                    android.util.Log.e("MailGun",
                            "Failed to send email: " + response.code() + " - " + response.message());
                    if (response.body() != null) {
                        android.util.Log.e("MailGun", "Response body: " + response.body().string());
                    }
                }
                return success;
            }
        } catch (IOException e) {
            android.util.Log.e("MailGun", "Error sending email to: " + userEmail, e);
            return false;
        }
    }

}