package com.example.basketballshoesandroidshop.Domain;

public class FeedbackModel {
    private String userId;
    private String itemId;
    private String orderId;
    private float rating;
    private String comment;
    private long createdAt;

    // Constructor rỗng cần thiết cho Firebase
    public FeedbackModel() {
    }

    public FeedbackModel(String userId, String itemId, String orderId, float rating, String comment, long createdAt) {
        this.userId = userId;
        this.itemId = itemId;
        this.orderId = orderId;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
