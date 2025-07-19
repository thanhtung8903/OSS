package com.example.oss.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.oss.R;
import com.example.oss.activity.CategoryManagementActivity;
import com.example.oss.activity.UserManagementActivity;
import com.google.android.material.card.MaterialCardView;
import com.example.oss.fragment.AdminProductManagementFragment;
import androidx.lifecycle.ViewModelProvider;
import android.widget.TextView;
import android.widget.ImageButton;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AdminFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AdminFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private MaterialCardView cardManageCategories;
    private MaterialCardView cardManageOrders;
    private MaterialCardView cardManageUsers;
    private MaterialCardView cardManageProducts;
    private MaterialCardView cardStatistics;
    private TextView tvAdminStats;
    private ImageButton btnBackAdmin;

    public AdminFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AdminFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AdminFragment newInstance(String param1, String param2) {
        AdminFragment fragment = new AdminFragment();
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
        View view = inflater.inflate(R.layout.fragment_admin, container, false);

        // Initialize views
        btnBackAdmin = view.findViewById(R.id.btn_back_admin);
        btnBackAdmin.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
        cardManageCategories = view.findViewById(R.id.card_manage_categories);
        cardManageOrders = view.findViewById(R.id.card_manage_orders);
        cardManageUsers = view.findViewById(R.id.card_manage_users);
        cardManageProducts = view.findViewById(R.id.card_manage_products);
        cardStatistics = view.findViewById(R.id.card_statistics);
        tvAdminStats = view.findViewById(R.id.tv_admin_stats);

        // Setup click listeners
        setupClickListeners();
        // Không load thống kê tổng quan ở header nữa
        // loadStatistics();
        return view;
    }

    private void setupClickListeners() {
        cardManageCategories.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CategoryManagementActivity.class);
            startActivity(intent);
        });
        cardManageOrders.setOnClickListener(v -> {
            OrderManagementFragment fragment = new OrderManagementFragment();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });
        cardManageUsers.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), UserManagementActivity.class);
            startActivity(intent);
        });
        cardManageProducts.setOnClickListener(v -> {
            AdminProductManagementFragment fragment = new AdminProductManagementFragment();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });
        cardStatistics.setOnClickListener(v -> {
            StatisticFragment fragment = new StatisticFragment();
            requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
        });
    }

    private void loadStatistics() {
        // TODO: Kết nối ViewModel thực tế để lấy số lượng user, sản phẩm, đơn hàng
        // Hiện tại demo số liệu giả lập
        tvAdminStats.setText("Tổng quan: 100 người dùng, 50 sản phẩm, 200 đơn hàng");
    }
}