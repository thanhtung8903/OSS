package com.example.oss.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.oss.R;
import com.example.oss.entity.Category;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryManagementAdapter extends ListAdapter<Category, CategoryManagementAdapter.CategoryViewHolder> {

    private OnItemClickListener listener;
    private Map<Integer, String> parentCategoryNames = new HashMap<>();

    public CategoryManagementAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<Category> DIFF_CALLBACK = new DiffUtil.ItemCallback<Category>() {
        @Override
        public boolean areItemsTheSame(@NonNull Category oldItem, @NonNull Category newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Category oldItem, @NonNull Category newItem) {
            return oldItem.getName().equals(newItem.getName()) &&
                   oldItem.getDescription().equals(newItem.getDescription()) &&
                   (oldItem.getParentId() == null ? newItem.getParentId() == null : 
                    oldItem.getParentId().equals(newItem.getParentId()));
        }
    };

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category_management, parent, false);
        return new CategoryViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = getItem(position);
        holder.bind(category);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setParentCategoryNames(Map<Integer, String> parentNames) {
        this.parentCategoryNames = parentNames;
        notifyDataSetChanged();
    }

    public class CategoryViewHolder extends RecyclerView.ViewHolder {
        private TextView tvCategoryName;
        private TextView tvCategoryDescription;
        private TextView tvParentCategory;
        private ImageButton btnEdit;
        private ImageButton btnDelete;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            tvCategoryDescription = itemView.findViewById(R.id.tvCategoryDescription);
            tvParentCategory = itemView.findViewById(R.id.tvParentCategory);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);

            // Set click listeners
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(getItem(position));
                }
            });

            btnEdit.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onEditClick(getItem(position));
                }
            });

            btnDelete.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onDeleteClick(getItem(position));
                }
            });
        }

        public void bind(Category category) {
            tvCategoryName.setText(category.getName());
            tvCategoryDescription.setText(category.getDescription());
            
            if (category.getParentId() != null) {
                String parentName = parentCategoryNames.get(category.getParentId());
                if (parentName != null) {
                    tvParentCategory.setText("Con của: " + parentName);
                } else {
                    tvParentCategory.setText("Danh mục con");
                }
                tvParentCategory.setVisibility(View.VISIBLE);
            } else {
                tvParentCategory.setText("Danh mục gốc");
                tvParentCategory.setVisibility(View.VISIBLE);
            }
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Category category);
        void onEditClick(Category category);
        void onDeleteClick(Category category);
    }
} 