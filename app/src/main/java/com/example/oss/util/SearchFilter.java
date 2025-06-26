package com.example.oss.util;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;

public class SearchFilter {

    // Price Range class
    public static class PriceRange {
        private BigDecimal minPrice;
        private BigDecimal maxPrice;

        public PriceRange() {
            this.minPrice = BigDecimal.ZERO;
            this.maxPrice = new BigDecimal("100000000"); // 100 triệu
        }

        public PriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
            this.minPrice = minPrice != null ? minPrice : BigDecimal.ZERO;
            this.maxPrice = maxPrice != null ? maxPrice : new BigDecimal("100000000");
        }

        public BigDecimal getMinPrice() {
            return minPrice;
        }

        public void setMinPrice(BigDecimal minPrice) {
            this.minPrice = minPrice;
        }

        public BigDecimal getMaxPrice() {
            return maxPrice;
        }

        public void setMaxPrice(BigDecimal maxPrice) {
            this.maxPrice = maxPrice;
        }

        public boolean isDefault() {
            return minPrice.equals(BigDecimal.ZERO) &&
                    maxPrice.compareTo(new BigDecimal("100000000")) >= 0;
        }

        @Override
        public String toString() {
            if (isDefault())
                return "Tất cả giá";
            return String.format("%,.0f₫ - %,.0f₫",
                    minPrice.doubleValue(), maxPrice.doubleValue());
        }
    }

    // Sort Options enum
    public enum SortOption {
        NAME_ASC("name_asc", "Tên: A-Z"),
        NAME_DESC("name_desc", "Tên: Z-A"),
        PRICE_ASC("price_asc", "Giá: Thấp đến cao"),
        PRICE_DESC("price_desc", "Giá: Cao đến thấp"),
        STOCK_FIRST("stock_first", "Còn hàng trước"),
        NEWEST_FIRST("newest_first", "Mới nhất");

        private final String value;
        private final String displayName;

        SortOption(String value, String displayName) {
            this.value = value;
            this.displayName = displayName;
        }

        public String getValue() {
            return value;
        }

        public String getDisplayName() {
            return displayName;
        }

        public static SortOption fromValue(String value) {
            for (SortOption option : values()) {
                if (option.value.equals(value)) {
                    return option;
                }
            }
            return NAME_ASC; // default
        }
    }

    // Complete Filter State
    public static class FilterState {
        private String searchQuery = "";
        private Set<Integer> categoryIds = new HashSet<>();
        private PriceRange priceRange = new PriceRange();
        private SortOption sortOption = SortOption.NAME_ASC;
        private boolean inStockOnly = false;

        public FilterState() {
        }

        // Getters and setters
        public String getSearchQuery() {
            return searchQuery;
        }

        public void setSearchQuery(String searchQuery) {
            this.searchQuery = searchQuery != null ? searchQuery : "";
        }

        public Set<Integer> getCategoryIds() {
            return categoryIds;
        }

        public void setCategoryIds(Set<Integer> categoryIds) {
            this.categoryIds = categoryIds != null ? categoryIds : new HashSet<>();
        }

        public PriceRange getPriceRange() {
            return priceRange;
        }

        public void setPriceRange(PriceRange priceRange) {
            this.priceRange = priceRange != null ? priceRange : new PriceRange();
        }

        public SortOption getSortOption() {
            return sortOption;
        }

        public void setSortOption(SortOption sortOption) {
            this.sortOption = sortOption != null ? sortOption : SortOption.NAME_ASC;
        }

        public boolean isInStockOnly() {
            return inStockOnly;
        }

        public void setInStockOnly(boolean inStockOnly) {
            this.inStockOnly = inStockOnly;
        }

        // Helper methods
        public boolean hasActiveFilters() {
            return !categoryIds.isEmpty() ||
                    !priceRange.isDefault() ||
                    inStockOnly ||
                    sortOption != SortOption.NAME_ASC;
        }

        public void clearAllFilters() {
            categoryIds.clear();
            priceRange = new PriceRange();
            sortOption = SortOption.NAME_ASC;
            inStockOnly = false;
        }

        public List<Integer> getCategoryIdsList() {
            return new ArrayList<>(categoryIds);
        }
    }
}