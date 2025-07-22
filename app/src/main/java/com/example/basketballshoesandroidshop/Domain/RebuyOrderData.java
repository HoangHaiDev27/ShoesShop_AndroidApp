package com.example.basketballshoesandroidshop.Domain;

import java.io.Serializable;
import java.util.List;

public class RebuyOrderData implements Serializable {
    private String originalOrderId;
    private List<OrderItemModel> items;
    private String shippingAddress;
    private String paymentMethod;
    private double originalTotalPrice;
    private double currentTotalPrice;
    private boolean hasChanges;

    public RebuyOrderData() {}

    public RebuyOrderData(OrderModel originalOrder) {
        this.originalOrderId = originalOrder.getOrderId();
        this.items = originalOrder.getItems();
        this.shippingAddress = originalOrder.getShippingAddress();
        this.paymentMethod = originalOrder.getPaymentMethod();
        this.originalTotalPrice = originalOrder.getTotalPrice();
        this.hasChanges = false;
        calculateCurrentTotalPrice();
    }

    private void calculateCurrentTotalPrice() {
        if (items != null) {
            double total = 0;
            for (OrderItemModel item : items) {
                total += item.getPrice() * item.getQuantity();
            }
            this.currentTotalPrice = total;
            this.hasChanges = (Math.abs(originalTotalPrice - currentTotalPrice) > 0.01);
        }
    }

    // Getters and Setters
    public String getOriginalOrderId() { return originalOrderId; }
    public void setOriginalOrderId(String originalOrderId) { this.originalOrderId = originalOrderId; }

    public List<OrderItemModel> getItems() { return items; }
    public void setItems(List<OrderItemModel> items) {
        this.items = items;
        calculateCurrentTotalPrice();
    }

    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public double getOriginalTotalPrice() { return originalTotalPrice; }
    public void setOriginalTotalPrice(double originalTotalPrice) { this.originalTotalPrice = originalTotalPrice; }

    public double getCurrentTotalPrice() { return currentTotalPrice; }
    public void setCurrentTotalPrice(double currentTotalPrice) { this.currentTotalPrice = currentTotalPrice; }

    public boolean hasChanges() { return hasChanges; }
    public void setHasChanges(boolean hasChanges) { this.hasChanges = hasChanges; }
}