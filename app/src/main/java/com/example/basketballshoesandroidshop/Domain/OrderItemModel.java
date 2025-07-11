package com.example.basketballshoesandroidshop.Domain;

public class OrderItemModel {
    private String itemId;
    private String title;
    private int quantity;
    private double price;
    private String size;
    private String picUrl;

    // Constructor rỗng cần thiết cho Firestore
    public OrderItemModel() {}

    // Getters and Setters
    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }

    public String getPicUrl() { return picUrl; }
    public void setPicUrl(String picUrl) { this.picUrl = picUrl; }
}
