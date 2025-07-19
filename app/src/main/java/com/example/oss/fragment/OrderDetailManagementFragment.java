package com.example.oss.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.oss.R;
import com.example.oss.adapter.OrderManagementOrderDetailAdapter;
import com.example.oss.bean.OrderDisplay;
import com.example.oss.dialog.UpdateOrderManagementStatusDialog;
import com.example.oss.viewmodel.OrderManagementViewModel;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class OrderDetailManagementFragment extends Fragment {
    private static final String ARG_ORDER = "order";
    private OrderDisplay order;

    private TextView tvOrderId, tvOrderDate, tvOrderStatus;
    private TextView tvCustomerName, tvCustomerEmail, tvCustomerPhone;
    private RecyclerView rvOrderItems;
    private TextView tvTotalAmount, tvPaymentMethod;
    //private Button btnUpdateStatus;
    private ImageView btnBack;

    public OrderDetailManagementFragment() {

    }

    public static OrderDetailManagementFragment newInstance(OrderDisplay order) {
        OrderDetailManagementFragment fragment = new OrderDetailManagementFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_ORDER, order);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            order = (OrderDisplay) getArguments().getSerializable(ARG_ORDER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_order_detail_management, container, false);
        initViews(view);
        bindDataToViews();
        return view;
    }

    private void initViews(View view){
        tvOrderId = view.findViewById(R.id.tv_order_id_header);
        tvOrderDate = view.findViewById(R.id.tv_order_date_header);
        tvOrderStatus = view.findViewById(R.id.tv_order_status_header);

        tvCustomerName = view.findViewById(R.id.tv_customer_name_detail);
        tvCustomerEmail = view.findViewById(R.id.tv_customer_email);
        tvCustomerPhone = view.findViewById(R.id.tv_customer_phone);

        rvOrderItems = view.findViewById(R.id.rv_order_items);
        rvOrderItems.setLayoutManager(new LinearLayoutManager(getContext()));

        tvTotalAmount = view.findViewById(R.id.tv_total_amount_detail);
        tvPaymentMethod = view.findViewById(R.id.tv_payment_method_detail);


        //btnUpdateStatus = view.findViewById(R.id.btn_update_status_detail);
        btnBack = view.findViewById(R.id.btn_back);
    }

    private void bindDataToViews(){
        if (order == null) return;

        tvOrderId.setText("Đơn hàng: " + String.valueOf(order.getOrderId()));
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        tvOrderDate.setText("Ngày đặt: " + sdf.format(order.getOrderDate()));
        tvOrderStatus.setText("Trạng thái: " + order.getOrderStatus());

        tvCustomerName.setText(order.getCustomerName());
        tvCustomerEmail.setText(order.getCustomerEmail());
        tvCustomerPhone.setText(order.getCustomerPhone());

        rvOrderItems.setAdapter(new OrderManagementOrderDetailAdapter(order.getProductList()));
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        tvTotalAmount.setText(nf.format(order.getTotalAmount()));

        tvPaymentMethod.setText(order.getPaymentMethod());

        btnBack.setOnClickListener(v -> requireActivity().onBackPressed());

//        btnUpdateStatus.setOnClickListener(v -> {
//
//        });

    }
}