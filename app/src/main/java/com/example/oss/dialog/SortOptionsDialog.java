package com.example.oss.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.example.oss.R;
import com.example.oss.util.SearchFilter;

public class SortOptionsDialog extends DialogFragment {

    private SearchFilter.SortOption currentSortOption;
    private OnSortOptionListener listener;
    private RadioGroup radioGroupSort;

    public interface OnSortOptionListener {
        void onSortOptionSelected(SearchFilter.SortOption sortOption);
    }

    public static SortOptionsDialog newInstance(SearchFilter.SortOption currentSortOption) {
        SortOptionsDialog dialog = new SortOptionsDialog();
        dialog.currentSortOption = currentSortOption != null ? currentSortOption : SearchFilter.SortOption.NAME_ASC;
        return dialog;
    }

    public void setOnSortOptionListener(OnSortOptionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_sort_options, null);

        radioGroupSort = view.findViewById(R.id.radio_group_sort);
        setupRadioButtons();

        return new AlertDialog.Builder(requireContext())
                .setTitle("Sắp xếp theo")
                .setView(view)
                .setPositiveButton("Áp dụng", (dialog, which) -> {
                    int selectedId = radioGroupSort.getCheckedRadioButtonId();
                    SearchFilter.SortOption selectedOption = getSortOptionFromId(selectedId);
                    if (listener != null) {
                        listener.onSortOptionSelected(selectedOption);
                    }
                })
                .setNegativeButton("Hủy", null)
                .create();
    }

    private void setupRadioButtons() {
        // Set current selection
        int currentId = getIdFromSortOption(currentSortOption);
        if (currentId != -1) {
            radioGroupSort.check(currentId);
        }
    }

    private int getIdFromSortOption(SearchFilter.SortOption sortOption) {
        switch (sortOption) {
            case NAME_ASC:
                return R.id.radio_name_asc;
            case NAME_DESC:
                return R.id.radio_name_desc;
            case PRICE_ASC:
                return R.id.radio_price_asc;
            case PRICE_DESC:
                return R.id.radio_price_desc;
            case STOCK_FIRST:
                return R.id.radio_stock_first;
            case NEWEST_FIRST:
                return R.id.radio_newest_first;
            default:
                return R.id.radio_name_asc;
        }
    }

    private SearchFilter.SortOption getSortOptionFromId(int id) {
        if (id == R.id.radio_name_asc)
            return SearchFilter.SortOption.NAME_ASC;
        else if (id == R.id.radio_name_desc)
            return SearchFilter.SortOption.NAME_DESC;
        else if (id == R.id.radio_price_asc)
            return SearchFilter.SortOption.PRICE_ASC;
        else if (id == R.id.radio_price_desc)
            return SearchFilter.SortOption.PRICE_DESC;
        else if (id == R.id.radio_stock_first)
            return SearchFilter.SortOption.STOCK_FIRST;
        else if (id == R.id.radio_newest_first)
            return SearchFilter.SortOption.NEWEST_FIRST;
        else
            return SearchFilter.SortOption.NAME_ASC;
    }
}