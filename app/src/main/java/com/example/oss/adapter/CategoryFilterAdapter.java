package com.example.oss.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.oss.R;
import com.example.oss.entity.Category;
import com.google.android.material.chip.Chip;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CategoryFilterAdapter extends RecyclerView.Adapter<CategoryFilterAdapter.CategoryChipViewHolder> {

    private List<Category> categories;
    private Set<Integer> selectedCategoryIds;
    private OnCategoryFilterChangeListener onFilterChangeListener;
    private boolean allowMultipleSelection;

    public interface OnCategoryFilterChangeListener {
        void onCategoryFilterChanged(Set<Integer> selectedCategoryIds);
    }

    public CategoryFilterAdapter(List<Category> categories,
            OnCategoryFilterChangeListener onFilterChangeListener,
            boolean allowMultipleSelection) {
        this.categories = categories != null ? categories : new ArrayList<>();
        this.selectedCategoryIds = new HashSet<>();
        this.onFilterChangeListener = onFilterChangeListener;
        this.allowMultipleSelection = allowMultipleSelection;
    }

    @NonNull
    @Override
    public CategoryChipViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category_chip, parent, false);
        return new CategoryChipViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryChipViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.bind(category);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public void updateCategories(List<Category> newCategories) {
        this.categories.clear();
        if (newCategories != null) {
            this.categories.addAll(newCategories);
        }
        notifyDataSetChanged();
    }

    public void clearSelection() {
        selectedCategoryIds.clear();
        notifyDataSetChanged();
        if (onFilterChangeListener != null) {
            onFilterChangeListener.onCategoryFilterChanged(new HashSet<>(selectedCategoryIds));
        }
    }

    public Set<Integer> getSelectedCategoryIds() {
        return new HashSet<>(selectedCategoryIds);
    }

    public void setSelectedCategories(Set<Integer> categoryIds) {
        this.selectedCategoryIds = categoryIds != null ? new HashSet<>(categoryIds) : new HashSet<>();
        notifyDataSetChanged();
    }

    class CategoryChipViewHolder extends RecyclerView.ViewHolder {
        private Chip chipCategory;

        public CategoryChipViewHolder(@NonNull View itemView) {
            super(itemView);
            chipCategory = itemView.findViewById(R.id.chip_category);
        }

        public void bind(Category category) {
            chipCategory.setText(category.getName());
            chipCategory.setChecked(selectedCategoryIds.contains(category.getId()));

            chipCategory.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    if (!allowMultipleSelection) {
                        // Single selection: clear other selections
                        selectedCategoryIds.clear();
                    }
                    selectedCategoryIds.add(category.getId());
                } else {
                    selectedCategoryIds.remove(category.getId());
                }

                // Notify listener
                if (onFilterChangeListener != null) {
                    onFilterChangeListener.onCategoryFilterChanged(new HashSet<>(selectedCategoryIds));
                }

                // Update other chips if single selection
                if (!allowMultipleSelection && isChecked) {
                    notifyDataSetChanged();
                }
            });
        }
    }
}