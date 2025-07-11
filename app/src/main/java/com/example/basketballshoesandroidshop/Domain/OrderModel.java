package com.example.basketballshoesandroidshop.Domain;

import com.google.firebase.database.Exclude;

import java.util.List;

public class OrderModel {

    // Các trường này sẽ không được ghi vào Firestore nhưng cần để xử lý logic
    @Exclude
    private String orderId;

    @Exclude
    private List<OrderItemModel> items;

    // Các trường tương ứng với JSON trong collection "Order"
    private String userId;
    private String orderDate;
    private String shippingAddress;
    private double totalPrice;
    private String paymentMethod;
    private String orderStatus;
    private boolean isPaid;
    private String paidAt;
    private boolean isDelivered;
    private String deliveredAt;
    private String location;

    // Constructor rỗng cần thiết cho Firestore
    public OrderModel() {}

    // Getters
    @Exclude
    public String getOrderId() { return orderId; }

    @Exclude
    public List<OrderItemModel> getItems() { return items; }

    public String getUserId() { return userId; }
    public String getOrderDate() { return orderDate; }
    public String getShippingAddress() { return shippingAddress; }
    public double getTotalPrice() { return totalPrice; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getOrderStatus() { return orderStatus; }
    public boolean isPaid() { return isPaid; }
    public String getPaidAt() { return paidAt; }
    public boolean isDelivered() { return isDelivered; }
    public String getDeliveredAt() { return deliveredAt; }
    public String getLocation() { return location; }

    // Setters
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public void setItems(List<OrderItemModel> items) { this.items = items; }

    public void setUserId(String userId) { this.userId = userId; }
    public void setOrderDate(String orderDate) { this.orderDate = orderDate; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }
    public void setPaid(boolean paid) { isPaid = paid; }
    public void setPaidAt(String paidAt) { this.paidAt = paidAt; }
    public void setDelivered(boolean delivered) { isDelivered = delivered; }
    public void setDeliveredAt(String deliveredAt) { this.deliveredAt = deliveredAt; }
    public void setLocation(String location) { this.location = location; }
}
