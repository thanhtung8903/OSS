package com.example.oss.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.example.oss.R;
import com.example.oss.util.SearchFilter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.RangeSlider;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class PriceFilterDialog extends DialogFragment {

    private SearchFilter.PriceRange currentPriceRange;
    private SearchFilter.PriceRange availablePriceRange;
    private OnPriceFilterListener listener;

    // UI components
    private RangeSlider rangeSlider;
    private EditText etMinPrice;
    private EditText etMaxPrice;
    private TextView tvMinLabel;
    private TextView tvMaxLabel;
    private MaterialButton btnReset;
    private MaterialButton btnApply;
    private MaterialButton btnCancel;

    public interface OnPriceFilterListener {
        void onPriceFilterApplied(SearchFilter.PriceRange priceRange);
    }

    public static PriceFilterDialog newInstance(SearchFilter.PriceRange currentPriceRange,
            SearchFilter.PriceRange availablePriceRange) {
        PriceFilterDialog dialog = new PriceFilterDialog();
        dialog.currentPriceRange = currentPriceRange != null ? currentPriceRange : new SearchFilter.PriceRange();
        dialog.availablePriceRange = availablePriceRange != null ? availablePriceRange : new SearchFilter.PriceRange();
        return dialog;
    }

    public void setOnPriceFilterListener(OnPriceFilterListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_price_filter, null);

        initViews(view);
        setupViews();
        setupListeners();

        return new AlertDialog.Builder(requireContext())
                .setTitle("Lọc theo giá")
                .setView(view)
                .create();
    }

    private void initViews(View view) {
        rangeSlider = view.findViewById(R.id.range_slider_price);
        etMinPrice = view.findViewById(R.id.et_min_price);
        etMaxPrice = view.findViewById(R.id.et_max_price);
        tvMinLabel = view.findViewById(R.id.tv_min_label);
        tvMaxLabel = view.findViewById(R.id.tv_max_label);
        btnReset = view.findViewById(R.id.btn_reset);
        btnApply = view.findViewById(R.id.btn_apply);
        btnCancel = view.findViewById(R.id.btn_cancel);
    }

    private void setupViews() {
        // Setup range slider
        float minValue = availablePriceRange.getMinPrice().floatValue();
        float maxValue = availablePriceRange.getMaxPrice().floatValue();

        rangeSlider.setValueFrom(minValue);
        rangeSlider.setValueTo(maxValue);
        rangeSlider.setValues(
                currentPriceRange.getMinPrice().floatValue(),
                currentPriceRange.getMaxPrice().floatValue());

        // Setup labels
        tvMinLabel.setText(formatPrice(availablePriceRange.getMinPrice()));
        tvMaxLabel.setText(formatPrice(availablePriceRange.getMaxPrice()));

        // Setup input fields
        etMinPrice.setText(String.valueOf(currentPriceRange.getMinPrice().intValue()));
        etMaxPrice.setText(String.valueOf(currentPriceRange.getMaxPrice().intValue()));
    }

    private void setupListeners() {
        // Range slider listener
        rangeSlider.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser) {
                List<Float> values = slider.getValues();
                if (values.size() >= 2) {
                    etMinPrice.setText(String.valueOf(values.get(0).intValue()));
                    etMaxPrice.setText(String.valueOf(values.get(1).intValue()));
                }
            }
        });

        // Input field listeners
        etMinPrice.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                updateSliderFromInputs();
            }
        });

        etMaxPrice.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                updateSliderFromInputs();
            }
        });

        // Button listeners
        btnReset.setOnClickListener(v -> resetToDefault());
        btnApply.setOnClickListener(v -> applyFilter());
        btnCancel.setOnClickListener(v -> dismiss());
    }

    private void updateSliderFromInputs() {
        try {
            float minPrice = Float.parseFloat(etMinPrice.getText().toString());
            float maxPrice = Float.parseFloat(etMaxPrice.getText().toString());

            // Validate range
            minPrice = Math.max(minPrice, availablePriceRange.getMinPrice().floatValue());
            maxPrice = Math.min(maxPrice, availablePriceRange.getMaxPrice().floatValue());

            if (minPrice <= maxPrice) {
                rangeSlider.setValues(minPrice, maxPrice);
            }
        } catch (NumberFormatException e) {
            // Reset to current slider values
            List<Float> values = rangeSlider.getValues();
            if (values.size() >= 2) {
                etMinPrice.setText(String.valueOf(values.get(0).intValue()));
                etMaxPrice.setText(String.valueOf(values.get(1).intValue()));
            }
        }
    }

    private void resetToDefault() {
        rangeSlider.setValues(
                availablePriceRange.getMinPrice().floatValue(),
                availablePriceRange.getMaxPrice().floatValue());
        etMinPrice.setText(String.valueOf(availablePriceRange.getMinPrice().intValue()));
        etMaxPrice.setText(String.valueOf(availablePriceRange.getMaxPrice().intValue()));
    }

    private void applyFilter() {
        List<Float> values = rangeSlider.getValues();
        if (values.size() >= 2 && listener != null) {
            SearchFilter.PriceRange newPriceRange = new SearchFilter.PriceRange(
                    new BigDecimal(values.get(0).toString()),
                    new BigDecimal(values.get(1).toString()));
            listener.onPriceFilterApplied(newPriceRange);
        }
        dismiss();
    }

    private String formatPrice(BigDecimal price) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        String formatted = formatter.format(price);
        return formatted.replace("₫", "").trim() + "₫";
    }
}