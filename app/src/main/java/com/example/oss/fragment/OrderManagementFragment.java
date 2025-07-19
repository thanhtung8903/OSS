package com.example.oss.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.oss.R;
import com.example.oss.adapter.OrderManagementAdapter;
import com.example.oss.viewmodel.OrderManagementViewModel;
import com.google.android.material.chip.Chip;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link OrderManagementFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OrderManagementFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private RecyclerView recyclerOrders;
    private OrderManagementAdapter adapter;
    private OrderManagementViewModel orderViewModel;
    private ImageButton btnBackOrderManagement;
    private TextView tvTotalOrders, tvPendingOrders, tvCompletedOrders;

    private Chip chipAll, chipPending, chipCompleted;



    public OrderManagementFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment OrderManagementFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static OrderManagementFragment newInstance(String param1, String param2) {
        OrderManagementFragment fragment = new OrderManagementFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_order_management, container, false);
        View view = inflater.inflate(R.layout.fragment_order_management, container, false);

        tvTotalOrders = view.findViewById(R.id.tv_total_orders);
        tvPendingOrders = view.findViewById(R.id.tv_pending_orders);
        tvCompletedOrders = view.findViewById(R.id.tv_completed_orders);

        recyclerOrders = view.findViewById(R.id.rv_orders);
        recyclerOrders.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new OrderManagementAdapter(null);
        recyclerOrders.setAdapter(adapter);

        orderViewModel = new ViewModelProvider(this).get(OrderManagementViewModel.class);

        orderViewModel.loadAllOrders();

        orderViewModel.getOrderDisplays().observe(getViewLifecycleOwner(), orderData -> {
            adapter.updateData(orderData.getOrderDisplays());
        });

        orderViewModel.getAllOrders().observe(getViewLifecycleOwner(), orderData -> {
            if (orderData != null){
                int total = orderData.getOrderDisplays().size();
                int pending = 0;
                int completed = 0;

                for (com.example.oss.bean.OrderDisplay order : orderData.getOrderDisplays()) {
                    String status = order.getOrderStatus().toLowerCase(); // Hoặc getOrderStatus()
                    if (status.contains("chờ") || status.contains("pending")) {
                        pending++;
                    } else if (status.contains("hoàn") || status.contains("delivered")) {
                        completed++;
                    }
                }

                tvTotalOrders.setText(String.valueOf(total));
                tvPendingOrders.setText(String.valueOf(pending));
                tvCompletedOrders.setText(String.valueOf(completed));
            }
        });

        btnBackOrderManagement = view.findViewById(R.id.btn_back_order_management);
        btnBackOrderManagement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        chipAll = view.findViewById(R.id.chip_all);
        chipPending = view.findViewById(R.id.chip_pending);
        chipCompleted = view.findViewById(R.id.chip_completed);

        chipAll.setOnClickListener(v -> orderViewModel.setFilterStatus("Tất cả"));
        chipPending.setOnClickListener(v -> orderViewModel.setFilterStatus("Chờ xử lý"));
        chipCompleted.setOnClickListener(v -> orderViewModel.setFilterStatus("Hoàn thành"));
// Thêm nếu có các chip khác như:
        view.findViewById(R.id.chip_processing).setOnClickListener(v -> orderViewModel.setFilterStatus("Đang xử lý"));
        view.findViewById(R.id.chip_shipped).setOnClickListener(v -> orderViewModel.setFilterStatus("Đã giao hàng"));
        view.findViewById(R.id.chip_cancelled).setOnClickListener(v -> orderViewModel.setFilterStatus("Đã hủy"));


        return view;
    }
}