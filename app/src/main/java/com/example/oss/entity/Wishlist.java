package com.example.oss.entity;

import androidx.room.Entity;
import androidx.room.ColumnInfo;
import androidx.room.ForeignKey;
import androidx.room.Index;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.util.Date;

@Entity(tableName = "wishlist", primaryKeys = { "user_id", "product_id" }, foreignKeys = {
        @ForeignKey(entity = User.class, parentColumns = "id", childColumns = "user_id", onDelete = ForeignKey.CASCADE),
        @ForeignKey(entity = Product.class, parentColumns = "id", childColumns = "product_id", onDelete = ForeignKey.CASCADE)
}, indices = { @Index("user_id"), @Index("product_id") })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wishlist {
    @ColumnInfo(name = "user_id")
    private int userId;

    @ColumnInfo(name = "product_id")
    private int productId;

    @ColumnInfo(name = "added_at")
    @Builder.Default
    private Date addedAt = new Date();

    // Constructor without addedAt (for creating new wishlist items)
    public Wishlist(int userId, int productId) {
        this.userId = userId;
        this.productId = productId;
        this.addedAt = new Date();
    }
}