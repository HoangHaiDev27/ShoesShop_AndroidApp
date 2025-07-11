package com.example.basketballshoesandroidshop.Domain;

import java.io.Serializable;

public class WishlistModel implements Serializable {
    private String itemId;
    private long addedAt;

    // Constructor rỗng cần thiết cho Firebase
    public WishlistModel() {
    }

    public WishlistModel(String itemId, long addedAt) {
        this.itemId = itemId;
        this.addedAt = addedAt;
    }

    // Getters and Setters
    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public long getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(long addedAt) {
        this.addedAt = addedAt;
    }
} 