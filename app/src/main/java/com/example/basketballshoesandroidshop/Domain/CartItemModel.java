package com.example.basketballshoesandroidshop.Domain;

public class CartItemModel {
    private String itemId;
    private String color;
    private String size;
    private double price;
    private int quantity;
    public CartItemModel(String itemId, double price, int quantity, String size, String color) {
        this.itemId = itemId;
        this.price = price;
        this.quantity = quantity;
        this.size = size;
        this.color = color;
    }
    public CartItemModel() {} // Firebase cần constructor rỗng

    // Getter & Setter
    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}

