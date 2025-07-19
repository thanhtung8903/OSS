package com.example.oss.bean;

import com.example.oss.dao.OrderItemDao;
import com.example.oss.entity.OrderItem;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class OrderDisplay implements Serializable {
    public int orderId;
    public Date orderDate;
    public String orderStatus;
    public String customerName;
    public String customerEmail;
    public String customerPhone;
    public List<OrderItemDao.OrderItemWithProduct> productList;
    public String productSummary; // Ví dụ: "Sản phẩm A (2), Sản phẩm B (1)"
    public BigDecimal totalAmount;

    public List<OrderItemDao.OrderItemWithProduct> getProductList() {
        return productList;
    }

    public void setProductList(List<OrderItemDao.OrderItemWithProduct> productList) {
        this.productList = productList;
    }

    public String paymentMethod;
    public int itemCount;

    public OrderDisplay() {
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
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
