package com.example.oss.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.oss.R;
import com.example.oss.entity.Review;
import com.example.oss.util.SessionManager;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private List<Review> reviews;
    private List<String> userNames; // We'll need to get user names separately
    private OnReviewActionListener listener;
    private SessionManager sessionManager;

    // Interface for review actions
    public interface OnReviewActionListener {
        void onEditReview(Review review);

        void onDeleteReview(Review review);
    }

    public ReviewAdapter(List<Review> reviews, List<String> userNames,
            OnReviewActionListener listener, SessionManager sessionManager) {
        this.reviews = reviews != null ? reviews : new ArrayList<>();
        this.userNames = userNames != null ? userNames : new ArrayList<>();
        this.listener = listener;
        this.sessionManager = sessionManager;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        android.util.Log.d("ReviewAdapter", "onCreateViewHolder called");
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviews.get(position);
        holder.bind(review, position);
    }

    @Override
    public int getItemCount() {
        android.util.Log.d("ReviewAdapter", "getItemCount called, returning: " + reviews.size());
        return reviews.size();
    }

    public void updateReviews(List<Review> newReviews) {
        android.util.Log.d("ReviewAdapter", "updateReviews called with " + newReviews.size() + " reviews");
        this.reviews.clear();
        this.reviews.addAll(newReviews);
        android.util.Log.d("ReviewAdapter", "reviews.size after update: " + reviews.size());
        notifyDataSetChanged();
    }

    public void updateUserNames(List<String> newUserNames) {
        this.userNames.clear();
        this.userNames.addAll(newUserNames);
        notifyDataSetChanged();
    }

    public void updateReviewsAndUserNames(List<Review> newReviews, List<String> newUserNames) {
        android.util.Log.d("ReviewAdapter", "updateReviewsAndUserNames called with " +
                (newReviews != null ? newReviews.size() : "null") + " reviews and " +
                (newUserNames != null ? newUserNames.size() : "null") + " user names");

        if (newReviews != null) {
            this.reviews.clear();
            this.reviews.addAll(newReviews);
            android.util.Log.d("ReviewAdapter", "Reviews updated, new size: " + this.reviews.size());
        }

        if (newUserNames != null) {
            this.userNames.clear();
            this.userNames.addAll(newUserNames);
            android.util.Log.d("ReviewAdapter", "User names updated, new size: " + this.userNames.size());
        }

        android.util.Log.d("ReviewAdapter", "About to call notifyDataSetChanged()");
        notifyDataSetChanged();
        android.util.Log.d("ReviewAdapter", "notifyDataSetChanged() completed");
    }

    class ReviewViewHolder extends RecyclerView.ViewHolder {

        private TextView tvReviewerName, tvReviewDate, tvRatingNumber, tvReviewComment;
        private ImageView[] stars = new ImageView[5];
        private LinearLayout layoutReviewActions;
        private ImageButton btnEditReview, btnDeleteReview;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);

            tvReviewerName = itemView.findViewById(R.id.tv_reviewer_name);
            tvReviewDate = itemView.findViewById(R.id.tv_review_date);
            tvRatingNumber = itemView.findViewById(R.id.tv_rating_number);
            tvReviewComment = itemView.findViewById(R.id.tv_review_comment);
            layoutReviewActions = itemView.findViewById(R.id.layout_review_actions);
            btnEditReview = itemView.findViewById(R.id.btn_edit_review);
            btnDeleteReview = itemView.findViewById(R.id.btn_delete_review);

            // Initialize stars array
            stars[0] = itemView.findViewById(R.id.star_1);
            stars[1] = itemView.findViewById(R.id.star_2);
            stars[2] = itemView.findViewById(R.id.star_3);
            stars[3] = itemView.findViewById(R.id.star_4);
            stars[4] = itemView.findViewById(R.id.star_5);
        }

        public void bind(Review review, int position) {
            // Debug log
            android.util.Log.d("ReviewAdapter", "Binding review " + position +
                    ": rating=" + review.getRating() + ", comment=" + review.getComment() +
                    ", userNames.size=" + userNames.size());

            // Set user name
            if (position < userNames.size() && !userNames.get(position).isEmpty()) {
                tvReviewerName.setText(userNames.get(position));
            } else {
                tvReviewerName.setText("Người dùng " + review.getUserId());
            }

            // Set review date
            tvReviewDate.setText(formatDate(review.getCreatedAt()));

            // Set rating number
            tvRatingNumber.setText(String.valueOf(review.getRating()));

            // Set rating stars
            setStarRating(review.getRating());

            // Set comment
            String comment = review.getComment();
            if (comment != null && !comment.trim().isEmpty()) {
                tvReviewComment.setText(comment);
                tvReviewComment.setVisibility(View.VISIBLE);
            } else {
                tvReviewComment.setText("Người dùng chưa để lại bình luận");
                tvReviewComment.setVisibility(View.VISIBLE);
                tvReviewComment.setTextColor(0xFF888888); // Gray color for placeholder
            }

            // Show/hide action buttons if this is current user's review
            boolean isCurrentUserReview = false;
            if (sessionManager != null) {
                SessionManager.SessionUser currentUser = sessionManager.getCurrentUser();
                isCurrentUserReview = currentUser != null &&
                        currentUser.getId() == review.getUserId();
            }

            if (isCurrentUserReview) {
                layoutReviewActions.setVisibility(View.VISIBLE);
                setupActionButtons(review);
            } else {
                layoutReviewActions.setVisibility(View.GONE);
            }
        }

        private void setStarRating(int rating) {
            for (int i = 0; i < stars.length; i++) {
                if (i < rating) {
                    stars[i].setImageResource(R.drawable.ic_favorite);
                    stars[i].setColorFilter(itemView.getContext().getResources().getColor(R.color.primary));
                } else {
                    stars[i].setImageResource(R.drawable.ic_favorite_border);
                    stars[i].setColorFilter(itemView.getContext().getResources().getColor(android.R.color.darker_gray));
                }
            }
        }

        private void setupActionButtons(Review review) {
            btnEditReview.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditReview(review);
                }
            });

            btnDeleteReview.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteReview(review);
                }
            });
        }

        private String formatDate(Date date) {
            if (date == null)
                return "";

            long now = System.currentTimeMillis();
            long reviewTime = date.getTime();
            long diffInMillis = now - reviewTime;

            long diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis);
            long diffInHours = TimeUnit.MILLISECONDS.toHours(diffInMillis);
            long diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis);

            if (diffInMinutes < 60) {
                return diffInMinutes + " phút trước";
            } else if (diffInHours < 24) {
                return diffInHours + " giờ trước";
            } else if (diffInDays < 7) {
                return diffInDays + " ngày trước";
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                return sdf.format(date);
            }
        }
    }
}