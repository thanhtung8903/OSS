package com.example.oss.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.oss.R;
import com.example.oss.bean.OrderDisplay;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.NumberFormat;
import java.util.Locale;


public class UpdateOrderManagementStatusDialog extends DialogFragment {

    private OrderDisplay order;
    private OnStatusUpdatedListener listener;

    public interface OnStatusUpdatedListener{
        void onStatusUpdated(OrderDisplay order, String newStatus, String note);
    }

    public UpdateOrderManagementStatusDialog(OrderDisplay order, OnStatusUpdatedListener listener){
        this.order = order;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_update_order_management_status, null);

        TextView tvOrderId = view.findViewById(R.id.tv_dialog_order_id);
        TextView tvCustomerName = view.findViewById(R.id.tv_dialog_customer_name);
        TextView tvTotalAmount = view.findViewById(R.id.tv_dialog_total_amount);
        TextView tvCurrentStatus = view.findViewById(R.id.tv_current_status);
        RadioGroup radioGroup = view.findViewById(R.id.radio_group_status);
        for (int i = 0; i < radioGroup.getChildCount(); i++) {
            View child = radioGroup.getChildAt(i);
            if (child instanceof RadioButton) {
                RadioButton radioButton = (RadioButton) child;
                String statusTag = String.valueOf(radioButton.getTag()); // lấy tag gán trong XML
                if (statusTag.equalsIgnoreCase(order.orderStatus)) {
                    radioButton.setChecked(true); // check radio button tương ứng
                    break;
                }
            }
        }
        TextInputEditText etNote = view.findViewById(R.id.et_status_note);

        tvOrderId.setText("Đơn hàng #" + order.orderId);
        tvCustomerName.setText("Khách hàng: " + order.customerName);
        tvTotalAmount.setText("Tổng tiền: " + NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(order.totalAmount));
        tvCurrentStatus.setText(order.orderStatus);

        MaterialButton btnCancel = view.findViewById(R.id.btn_cancel_status);
        MaterialButton btnUpdate = view.findViewById(R.id.btn_update_status);

        btnCancel.setOnClickListener(v -> dismiss());

        btnUpdate.setOnClickListener(v -> {
            int selectedId = radioGroup.getCheckedRadioButtonId();
            if (selectedId != -1) {
                RadioButton rb = view.findViewById(selectedId);
                String newStatus = rb.getText().toString();
                String note = etNote.getText().toString();
                listener.onStatusUpdated(order, newStatus, note);
                dismiss();
            }
        });

        return new AlertDialog.Builder(requireContext())
                .setView(view)
                .create();
    }
}