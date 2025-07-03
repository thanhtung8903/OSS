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
import com.example.oss.entity.User;

public class UserManagementAdapter extends ListAdapter<User, UserManagementAdapter.UserViewHolder> {

    private OnItemClickListener listener;

    public UserManagementAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<User> DIFF_CALLBACK = new DiffUtil.ItemCallback<User>() {
        @Override
        public boolean areItemsTheSame(@NonNull User oldItem, @NonNull User newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull User oldItem, @NonNull User newItem) {
            return oldItem.equals(newItem);
        }
    };

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_management, parent, false);
        return new UserViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        private TextView tvName, tvEmail, tvRole, tvStatus;
        private ImageButton btnDetail, btnEditStatus;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvUserName);
            tvEmail = itemView.findViewById(R.id.tvUserEmail);
            tvRole = itemView.findViewById(R.id.tvUserRole);
            tvStatus = itemView.findViewById(R.id.tvUserStatus);
            btnDetail = itemView.findViewById(R.id.btnDetail);
            btnEditStatus = itemView.findViewById(R.id.btnEditStatus);

            btnDetail.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (listener != null && pos != RecyclerView.NO_POSITION) {
                    listener.onDetailClick(getItem(pos));
                }
            });

            btnEditStatus.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (listener != null && pos != RecyclerView.NO_POSITION) {
                    listener.onEditStatusClick(getItem(pos));
                }
            });
        }

        public void bind(User user) {
            tvName.setText(user.getFullName());
            tvEmail.setText(user.getEmail());
            tvRole.setText(user.getRole());
            tvStatus.setText(user.getStatus());
        }
    }

    public interface OnItemClickListener {
        void onDetailClick(User user);
        void onEditStatusClick(User user);
    }
} 