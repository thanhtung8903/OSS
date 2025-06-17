package com.example.oss.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.ForeignKey;
import androidx.room.Index;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.util.Date;

@Entity(tableName = "reviews", foreignKeys = {
        @ForeignKey(entity = User.class, parentColumns = "id", childColumns = "user_id", onDelete = ForeignKey.CASCADE),
        @ForeignKey(entity = Product.class, parentColumns = "id", childColumns = "product_id", onDelete = ForeignKey.CASCADE)
}, indices = { @Index("user_id"), @Index("product_id") })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "user_id")
    private int userId;

    @ColumnInfo(name = "product_id")
    private int productId;

    @ColumnInfo(name = "rating")
    private int rating; // 1-5 stars

    @ColumnInfo(name = "comment")
    private String comment;

    @ColumnInfo(name = "created_at")
    @Builder.Default
    private Date createdAt = new Date();

    // Constructor without id and createdAt (for creating new reviews)
    public Review(int userId, int productId, int rating, String comment) {
        this.userId = userId;
        this.productId = productId;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = new Date();
    }
}