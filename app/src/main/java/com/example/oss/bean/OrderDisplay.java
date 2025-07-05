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
}
