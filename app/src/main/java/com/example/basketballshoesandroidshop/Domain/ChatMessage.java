package com.example.basketballshoesandroidshop.Domain;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ChatMessage {
    private String messageId;
    private String senderId;
    private String senderName;
    private String message;
    private long timestamp;
    private MessageType messageType;
    private String imageUrl; // Nếu tin nhắn có hình ảnh
    private boolean isRead;

    // Enum cho loại tin nhắn
    public enum MessageType {
        USER,           // Tin nhắn từ user
        BOT,            // Tin nhắn từ bot
        SYSTEM,         // Tin nhắn hệ thống
        PRODUCT,        // Tin nhắn chứa sản phẩm
        TYPING          // Indicator đang gõ
    }

    // Constructors
    public ChatMessage() {
        this.timestamp = System.currentTimeMillis();
        this.isRead = false;
    }

    public ChatMessage(String senderId, String senderName, String message, MessageType messageType) {
        this.senderId = senderId;
        this.senderName = senderName;
        this.message = message;
        this.messageType = messageType;
        this.timestamp = System.currentTimeMillis();
        this.isRead = false;
    }

    // Static factory methods cho dễ tạo
    public static ChatMessage createUserMessage(String userId, String userName, String message) {
        return new ChatMessage(userId, userName, message, MessageType.USER);
    }

    public static ChatMessage createBotMessage(String message) {
        return new ChatMessage("bot_001", "Basketball Assistant", message, MessageType.BOT);
    }

    public static ChatMessage createSystemMessage(String message) {
        return new ChatMessage("system", "System", message, MessageType.SYSTEM);
    }

    // Getters và Setters
    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public MessageType getMessageType() { return messageType; }
    public void setMessageType(MessageType messageType) { this.messageType = messageType; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    // Helper methods
    public boolean isFromUser() {
        return messageType == MessageType.USER;
    }

    public boolean isFromBot() {
        return messageType == MessageType.BOT || messageType == MessageType.SYSTEM;
    }

    public boolean hasImage() {
        return imageUrl != null && !imageUrl.isEmpty();
    }

    public String getFormattedTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    public String getFormattedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    public boolean isSentToday() {
        long today = System.currentTimeMillis();
        long dayInMillis = 24 * 60 * 60 * 1000;
        return (today - timestamp) < dayInMillis;
    }
}