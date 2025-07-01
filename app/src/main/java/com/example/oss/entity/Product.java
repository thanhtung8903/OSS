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

import java.io.Serializable;
import java.math.BigDecimal;

@Entity(tableName = "products", foreignKeys = @ForeignKey(entity = Category.class, parentColumns = "id", childColumns = "category_id", onDelete = ForeignKey.CASCADE), indices = {
        @Index("category_id") })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "category_id")
    private int categoryId;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "price")
    private BigDecimal price;

    @ColumnInfo(name = "stock_quantity")
    private int stockQuantity;

    @ColumnInfo(name = "image_url")
    private String imageUrl;

    @ColumnInfo(name = "is_active")
    @Builder.Default
    private boolean isActive = true;

    // Constructor without id (for creating new products)
    public Product(int categoryId, String name, String description,
            BigDecimal price, int stockQuantity, String imageUrl, boolean isActive) {
        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.imageUrl = imageUrl;
        this.isActive = isActive;
    }
}