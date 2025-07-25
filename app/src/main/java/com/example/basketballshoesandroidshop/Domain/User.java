package com.example.basketballshoesandroidshop.Domain;

public class User {
    private String userId;
    private String name;
    private String email;
    private String password;
    private String phone;
    private String address;
    private String createdAt;

    // Constructor rỗng (bắt buộc cho Firebase)
    public User() {}

    // Constructor đầy đủ
    public User(String userId, String name, String email, String password, String phone, String address, String createdAt) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.address = address;
        this.createdAt = createdAt;
    }

    // Getters và Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}