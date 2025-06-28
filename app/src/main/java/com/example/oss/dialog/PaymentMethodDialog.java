package com.example.oss.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.example.oss.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

public class PaymentMethodDialog extends DialogFragment {

    private MaterialToolbar toolbar;
    private RadioGroup radioGroupPayment;
    private RadioButton radioCash, radioCard, radioTransfer;
    private MaterialButton btnConfirm, btnCancel;

    private OnPaymentMethodSelectedListener listener;
    private String selectedPaymentMethod = "cash"; // Default

    public interface OnPaymentMethodSelectedListener {
        void onPaymentMethodSelected(String paymentMethod);
    }

    public void setOnPaymentMethodSelectedListener(OnPaymentMethodSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_OSS);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_payment_method, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupToolbar();
        setupListeners();
        setupDefaultSelection();
    }

    private void initViews(View view) {
        toolbar = view.findViewById(R.id.toolbar);
        radioGroupPayment = view.findViewById(R.id.radio_group_payment);
        radioCash = view.findViewById(R.id.radio_cash);
        radioCard = view.findViewById(R.id.radio_card);
        radioTransfer = view.findViewById(R.id.radio_transfer);
        btnConfirm = view.findViewById(R.id.btn_confirm);
        btnCancel = view.findViewById(R.id.btn_cancel);
    }

    private void setupToolbar() {
        toolbar.setTitle("Chọn phương thức thanh toán");
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(v -> dismiss());
    }

    private void setupListeners() {
        radioGroupPayment.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_cash) {
                selectedPaymentMethod = "cash";
            } else if (checkedId == R.id.radio_card) {
                selectedPaymentMethod = "card";
            } else if (checkedId == R.id.radio_transfer) {
                selectedPaymentMethod = "transfer";
            }
        });

        btnConfirm.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPaymentMethodSelected(selectedPaymentMethod);
            }
            dismiss();
        });

        btnCancel.setOnClickListener(v -> dismiss());
    }

    private void setupDefaultSelection() {
        radioCash.setChecked(true);
        selectedPaymentMethod = "cash";
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
            int height = ViewGroup.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setLayout(width, height);
        }
    }
}