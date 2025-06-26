package com.example.oss.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.oss.R;
import com.example.oss.entity.Category;
import com.example.oss.util.ImageLoader;
import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<Category> categories;
    private OnCategoryClickListener onCategoryClickListener;

    // Interface for click listener
    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
    }

    public CategoryAdapter(List<Category> categories, OnCategoryClickListener onCategoryClickListener) {
        this.categories = categories != null ? categories : new ArrayList<>();
        this.onCategoryClickListener = onCategoryClickListener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.bind(category);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    // Update data
    public void updateCategories(List<Category> newCategories) {
        this.categories.clear();
        if (newCategories != null) {
            this.categories.addAll(newCategories);
        }
        notifyDataSetChanged();
    }

    // ViewHolder class
    class CategoryViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivCategoryIcon;
        private TextView tvCategoryName;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCategoryIcon = itemView.findViewById(R.id.iv_category_icon);
            tvCategoryName = itemView.findViewById(R.id.tv_category_name);
        }

        public void bind(Category category) {
            // Set category name
            tvCategoryName.setText(category.getName());

            // Load category icon với ImageLoader
            ImageLoader.loadCategoryIcon(
                    itemView.getContext(),
                    null,
                    ivCategoryIcon);

            // Set click listener
            itemView.setOnClickListener(v -> {
                if (onCategoryClickListener != null) {
                    onCategoryClickListener.onCategoryClick(category);
                }
            });
        }

        private int getCategoryIcon(String categoryName) {
            // Return appropriate icon based on category name
            if (categoryName.toLowerCase().contains("điện tử") ||
                    categoryName.toLowerCase().contains("công nghệ")) {
                return R.drawable.ic_electronics;
            } else if (categoryName.toLowerCase().contains("thời trang") ||
                    categoryName.toLowerCase().contains("quần áo")) {
                return R.drawable.ic_fashion;
            } else if (categoryName.toLowerCase().contains("sách")) {
                return R.drawable.ic_book;
            } else if (categoryName.toLowerCase().contains("nhà cửa") ||
                    categoryName.toLowerCase().contains("đời sống")) {
                return R.drawable.ic_home;
            } else if (categoryName.toLowerCase().contains("thể thao") ||
                    categoryName.toLowerCase().contains("du lịch")) {
                return R.drawable.ic_sports;
            } else {
                return R.drawable.ic_category_default;
            }
        }
    }
}