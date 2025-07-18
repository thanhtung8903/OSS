package com.example.oss.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.oss.R;
import com.example.oss.database.AppDatabase;
import com.google.android.material.appbar.MaterialToolbar;
import java.util.List;
import java.util.concurrent.Executors;

public class StatisticFragment extends Fragment {
    private MaterialToolbar toolbarStatistic;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistic, container, false);
        toolbarStatistic = view.findViewById(R.id.toolbar_statistic);
        toolbarStatistic.setNavigationOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
        TextView tvStats = view.findViewById(R.id.tv_stats);

        Executors.newSingleThreadExecutor().execute(() -> {
            Context context = requireContext().getApplicationContext();
            int userCount = AppDatabase.getDatabase(context).userDao().getUserCountSync();
            int productCount = AppDatabase.getDatabase(context).productDao().getAllActiveProductsSync().size();
            int orderCount = AppDatabase.getDatabase(context).orderDao().getAllOrdersSync().size();
            int categoryCount = AppDatabase.getDatabase(context).categoryDao().getCategoryCountSync();

            List<com.example.oss.entity.User> allUsers = AppDatabase.getDatabase(context).userDao().getAllUsers().getValue();
            int customerCount = 0;
            if (allUsers != null) {
                for (com.example.oss.entity.User user : allUsers) {
                    if (!"Admin".equalsIgnoreCase(user.getRole())) {
                        customerCount++;
                    }
                }
            } else {
                customerCount = userCount;
            }

            String stats = "Tổng số người dùng: " + customerCount +
                           "\nTổng số danh mục: " + categoryCount +
                           "\nTổng số sản phẩm: " + productCount +
                           "\nTổng số đơn hàng: " + orderCount;

            requireActivity().runOnUiThread(() -> tvStats.setText(stats));
        });

        return view;
    }
} 