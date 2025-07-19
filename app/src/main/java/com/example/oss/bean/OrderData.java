package com.example.oss.bean;

import java.util.List;

public class OrderData {
    private List<OrderDisplay> orderDisplays;
    private OrderStatistics statistics;

    public OrderData() {
    }

    public OrderData(List<OrderDisplay> orderDisplays, OrderStatistics statistics) {
        this.orderDisplays = orderDisplays;
        this.statistics = statistics;
    }

    public List<OrderDisplay> getOrderDisplays() {
        return orderDisplays;
    }

    public void setOrderDisplays(List<OrderDisplay> orderDisplays) {
        this.orderDisplays = orderDisplays;
    }

    public OrderStatistics getStatistics() {
        return statistics;
    }

    public void setStatistics(OrderStatistics statistics) {
        this.statistics = statistics;
    }
}
