package com.example.oss.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.oss.R;
import com.example.oss.viewmodel.ReviewViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class WriteReviewDialog extends DialogFragment {

    private MaterialToolbar toolbar;
    private ImageView[] stars = new ImageView[5];
    private TextView tvRatingDescription;
    private TextInputLayout tilComment;
    private TextInputEditText etComment;
    private MaterialButton btnCancel, btnSubmit;

    private ReviewViewModel reviewViewModel;
    private OnReviewSubmittedListener listener;
    private int productId;
    private int currentRating = 0;

    private static final String ARG_PRODUCT_ID = "product_id";

    // Interface for callback
    public interface OnReviewSubmittedListener {
        void onReviewSubmitted();
    }

    public static WriteReviewDialog newInstance(int productId) {
        WriteReviewDialog dialog = new WriteReviewDialog();
        Bundle args = new Bundle();
        args.putInt(ARG_PRODUCT_ID, productId);
        dialog.setArguments(args);
        return dialog;
    }

    public void setOnReviewSubmittedListener(OnReviewSubmittedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_OSS);

        if (getArguments() != null) {
            productId = getArguments().getInt(ARG_PRODUCT_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_write_review, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initViewModel();
        setupListeners();
        setupObservers();
    }

    private void initViews(View view) {
        toolbar = view.findViewById(R.id.toolbar);

        // Initialize stars array
        stars[0] = view.findViewById(R.id.star_1);
        stars[1] = view.findViewById(R.id.star_2);
        stars[2] = view.findViewById(R.id.star_3);
        stars[3] = view.findViewById(R.id.star_4);
        stars[4] = view.findViewById(R.id.star_5);

        tvRatingDescription = view.findViewById(R.id.tv_rating_description);
        tilComment = view.findViewById(R.id.til_comment);
        etComment = view.findViewById(R.id.et_comment);
        btnCancel = view.findViewById(R.id.btn_cancel);
        btnSubmit = view.findViewById(R.id.btn_submit);
    }

    private void initViewModel() {
        reviewViewModel = new ViewModelProvider(this).get(ReviewViewModel.class);
    }

    private void setupListeners() {
        toolbar.setNavigationOnClickListener(v -> dismiss());

        // Setup star click listeners
        for (int i = 0; i < stars.length; i++) {
            final int starIndex = i;
            stars[i].setOnClickListener(v -> setRating(starIndex + 1));
        }

        // Setup text change listener for comment
        etComment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateSubmitButton();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        btnCancel.setOnClickListener(v -> dismiss());

        btnSubmit.setOnClickListener(v -> submitReview());
    }

    private void setupObservers() {
        // Observe loading state
        reviewViewModel.getIsLoading().observe(this, isLoading -> {
            btnSubmit.setEnabled(!isLoading && isFormValid());
            if (isLoading) {
                btnSubmit.setText("Đang gửi...");
            } else {
                btnSubmit.setText("Gửi đánh giá");
            }
        });

        // Observe success message
        reviewViewModel.getSuccessMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                if (listener != null) {
                    listener.onReviewSubmitted();
                }
                reviewViewModel.clearSuccess();
                dismiss();
            }
        });

        // Observe error message
        reviewViewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                tilComment.setError(error);
                reviewViewModel.clearError();
            } else {
                tilComment.setError(null);
            }
        });
    }

    private void setRating(int rating) {
        currentRating = rating;

        // Update star icons
        for (int i = 0; i < stars.length; i++) {
            if (i < rating) {
                stars[i].setImageResource(R.drawable.ic_favorite);
                stars[i].setColorFilter(getResources().getColor(R.color.primary));
            } else {
                stars[i].setImageResource(R.drawable.ic_favorite_border);
                stars[i].setColorFilter(getResources().getColor(android.R.color.darker_gray));
            }
        }

        // Update rating description
        String[] ratingDescriptions = {
                "Chưa đánh giá",
                "Rất tệ",
                "Tệ",
                "Bình thường",
                "Tốt",
                "Rất tốt"
        };

        if (rating > 0 && rating <= ratingDescriptions.length - 1) {
            tvRatingDescription.setText(ratingDescriptions[rating]);
            tvRatingDescription.setTextColor(getResources().getColor(R.color.primary));
        } else {
            tvRatingDescription.setText(ratingDescriptions[0]);
            tvRatingDescription.setTextColor(getResources().getColor(R.color.on_surface_variant));
        }

        updateSubmitButton();
    }

    private void updateSubmitButton() {
        btnSubmit.setEnabled(isFormValid());
    }

    private boolean isFormValid() {
        return currentRating > 0 &&
                etComment.getText() != null &&
                etComment.getText().toString().trim().length() >= 10;
    }

    private void submitReview() {
        String comment = etComment.getText().toString().trim();

        if (currentRating <= 0) {
            tilComment.setError("Vui lòng chọn số sao đánh giá");
            return;
        }

        if (comment.length() < 10) {
            tilComment.setError("Bình luận phải có ít nhất 10 ký tự");
            return;
        }

        tilComment.setError(null);
        reviewViewModel.addReview(productId, currentRating, comment);
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }
}