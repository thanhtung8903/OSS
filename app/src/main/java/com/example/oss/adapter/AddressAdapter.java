package com.example.oss.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.oss.R;
import com.example.oss.entity.Address;
import java.util.ArrayList;
import java.util.List;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.AddressViewHolder> {

    private List<Address> addresses = new ArrayList<>();
    private OnAddressActionListener listener;
    private boolean isSelectionMode = false; // Để chọn địa chỉ trong checkout

    public interface OnAddressActionListener {
        void onAddressEdit(Address address);

        void onAddressDelete(Address address);

        void onAddressSetDefault(Address address);

        void onAddressSelect(Address address); // Cho checkout
    }

    public AddressAdapter(OnAddressActionListener listener) {
        this.listener = listener;
    }

    public AddressAdapter(OnAddressActionListener listener, boolean isSelectionMode) {
        this.listener = listener;
        this.isSelectionMode = isSelectionMode;
    }

    @NonNull
    @Override
    public AddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_address, parent, false);
        return new AddressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddressViewHolder holder, int position) {
        Address address = addresses.get(position);
        holder.bind(address);
    }

    @Override
    public int getItemCount() {
        return addresses.size();
    }

    public void updateAddresses(List<Address> newAddresses) {
        this.addresses.clear();
        this.addresses.addAll(newAddresses);
        notifyDataSetChanged();
    }

    class AddressViewHolder extends RecyclerView.ViewHolder {
        private TextView tvRecipientName, tvAddressType, tvPhoneNumber, tvFullAddress;
        private View layoutDefaultBadge;
        private ImageView ivEdit, ivDelete, ivSetDefault;
        private View layoutActions, btnSetDefault;

        public AddressViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRecipientName = itemView.findViewById(R.id.tv_receiver_name);
            tvAddressType = itemView.findViewById(R.id.tv_address_type);
            tvPhoneNumber = itemView.findViewById(R.id.tv_phone_number);
            tvFullAddress = itemView.findViewById(R.id.tv_full_address);
            layoutDefaultBadge = itemView.findViewById(R.id.layout_default);
            ivEdit = itemView.findViewById(R.id.iv_edit);
            ivDelete = itemView.findViewById(R.id.iv_delete);
            ivSetDefault = itemView.findViewById(R.id.iv_set_default);
            btnSetDefault = itemView.findViewById(R.id.btn_set_default);
            layoutActions = itemView;
        }

        public void bind(Address address) {
            tvRecipientName.setText(address.getReceiverName());
            tvPhoneNumber.setText(address.getPhoneNumber());

            String fullAddress = String.format("%s, %s, %s",
                    address.getStreetAddress(),
                    address.getDistrict(),
                    address.getCity());
            tvFullAddress.setText(fullAddress);

            // Set address type background
            String addressType = address.getAddressType();
            tvAddressType.setText(getAddressTypeText(addressType));
            tvAddressType.setBackgroundResource(getAddressTypeBackground(addressType));

            // Show/hide default badge
            layoutDefaultBadge.setVisibility(address.isDefault() ? View.VISIBLE : View.GONE);

            if (isSelectionMode) {
                // Hide action buttons in selection mode
                layoutActions.setVisibility(View.GONE);

                // Set click listener for selection
                itemView.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onAddressSelect(address);
                    }
                });
            } else {
                // Show action buttons in management mode
                layoutActions.setVisibility(View.VISIBLE);

                // Set action listeners
                ivEdit.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onAddressEdit(address);
                    }
                });

                ivDelete.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onAddressDelete(address);
                    }
                });

                // Set click listener cho cả button và icon
                View.OnClickListener setDefaultListener = v -> {
                    if (listener != null) {
                        if (address.isDefault()) {
                            // Không cho phép bỏ mặc định - hiển thị thông báo
                            android.widget.Toast.makeText(v.getContext(),
                                    "Không thể bỏ địa chỉ mặc định. Vui lòng chọn địa chỉ khác làm mặc định trước.",
                                    android.widget.Toast.LENGTH_SHORT).show();
                        } else {
                            listener.onAddressSetDefault(address);
                        }
                    }
                };

                ivSetDefault.setOnClickListener(setDefaultListener);
                btnSetDefault.setOnClickListener(setDefaultListener);

                // Thay đổi icon và text dựa trên trạng thái default
                TextView tvDefaultText = btnSetDefault.findViewById(android.R.id.text1);
                if (tvDefaultText == null) {
                    // Tìm TextView trong LinearLayout
                    if (btnSetDefault instanceof android.widget.LinearLayout) {
                        android.widget.LinearLayout layout = (android.widget.LinearLayout) btnSetDefault;
                        for (int i = 0; i < layout.getChildCount(); i++) {
                            if (layout.getChildAt(i) instanceof TextView) {
                                tvDefaultText = (TextView) layout.getChildAt(i);
                                break;
                            }
                        }
                    }
                }

                if (address.isDefault()) {
                    ivSetDefault.setImageResource(R.drawable.ic_favorite);
                    ivSetDefault.setColorFilter(itemView.getContext().getColor(R.color.error));
                    if (tvDefaultText != null) {
                        tvDefaultText.setText("Đã mặc định");
                        tvDefaultText.setTextColor(itemView.getContext().getColor(R.color.error));
                    }
                } else {
                    ivSetDefault.setImageResource(R.drawable.ic_favorite_border);
                    ivSetDefault.setColorFilter(itemView.getContext().getColor(R.color.on_surface_variant));
                    if (tvDefaultText != null) {
                        tvDefaultText.setText("Đặt mặc định");
                        tvDefaultText.setTextColor(itemView.getContext().getColor(R.color.on_surface_variant));
                    }
                }

                // Luôn hiển thị nút để người dùng có thể thấy trạng thái
                ivSetDefault.setVisibility(View.VISIBLE);
                btnSetDefault.setVisibility(View.VISIBLE);
            }
        }

        private String getAddressTypeText(String type) {
            switch (type.toLowerCase()) {
                case "home":
                    return "Nhà riêng";
                case "office":
                    return "Văn phòng";
                case "other":
                    return "Khác";
                default:
                    return "Khác";
            }
        }

        private int getAddressTypeBackground(String type) {
            switch (type.toLowerCase()) {
                case "home":
                    return R.drawable.address_type_home_background;
                case "office":
                    return R.drawable.address_type_office_background;
                case "other":
                    return R.drawable.address_type_other_background;
                default:
                    return R.drawable.address_type_background;
            }
        }
    }
}