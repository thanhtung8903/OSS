package com.example.oss.bean;

import java.math.BigDecimal;
import java.util.Date;

public class OrderDisplay {
    public int orderId;
    public Date orderDate;
    public String orderStatus;
    public String customerName;
    public String productSummary; // Ví dụ: "Sản phẩm A (2), Sản phẩm B (1)"
    public BigDecimal totalAmount;
    public String paymentMethod;
    public int itemCount;

    public OrderDisplay() {
    }

    public OrderDisplay(int orderId, Date orderDate, String orderStatus, String customerName, String productSummary, BigDecimal totalAmount, String paymentMethod, int itemCount) {
        this.orderId = orderId;
        this.orderDate = orderDate;
        this.orderStatus = orderStatus;
        this.customerName = customerName;
        this.productSummary = productSummary;
        this.totalAmount = totalAmount;
        this.paymentMethod = paymentMethod;
        this.itemCount = itemCount;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getProductSummary() {
        return productSummary;
    }

    public void setProductSummary(String productSummary) {
        this.productSummary = productSummary;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public int getItemCount() {
        return itemCount;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }
}
