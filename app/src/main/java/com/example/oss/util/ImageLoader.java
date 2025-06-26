package com.example.oss.util;

import android.content.Context;
import android.widget.ImageView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Callback;
import com.example.oss.R;

public class ImageLoader {

    /**
     * Load ảnh sản phẩm với fallback
     */
    public static void loadProductImage(Context context, String imageUrl, ImageView imageView) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            // Nếu không có URL, dùng placeholder mặc định
            imageView.setImageResource(R.drawable.ic_image_placeholder);
            return;
        }

        // Nếu là drawable resource (local image)
        if (imageUrl.startsWith("drawable://")) {
            String resourceName = imageUrl.replace("drawable://", "");
            int resourceId = getDrawableResourceId(context, resourceName);
            if (resourceId != 0) {
                imageView.setImageResource(resourceId);
                return;
            }
        }

        // Load ảnh từ URL với Picasso
        Picasso.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_image_placeholder) // Hiển thị khi đang load
                .error(R.drawable.ic_image_placeholder) // Hiển thị khi load lỗi
                .fit()
                .centerCrop()
                .into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        // Ảnh load thành công
                    }

                    @Override
                    public void onError() {
                        // Ảnh load lỗi, đã set error drawable
                    }
                });
    }

    /**
     * Load ảnh category với fallback
     */
    public static void loadCategoryIcon(Context context, String iconUrl, ImageView imageView) {
        if (iconUrl == null || iconUrl.isEmpty()) {
            imageView.setImageResource(R.drawable.ic_category_default);
            return;
        }

        // Nếu là tên icon resource
        int resourceId = getDrawableResourceId(context, iconUrl);
        if (resourceId != 0) {
            imageView.setImageResource(resourceId);
        } else {
            // Fallback nếu không tìm thấy resource
            imageView.setImageResource(R.drawable.ic_category_default);
        }
    }

    /**
     * Get drawable resource ID từ tên
     */
    private static int getDrawableResourceId(Context context, String resourceName) {
        try {
            return context.getResources().getIdentifier(
                    resourceName,
                    "drawable",
                    context.getPackageName());
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Preload ảnh để cache
     */
    public static void preloadImage(Context context, String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty() && imageUrl.startsWith("http")) {
            Picasso.with(context)
                    .load(imageUrl)
                    .fetch(); // Chỉ download không hiển thị
        }
    }
}
