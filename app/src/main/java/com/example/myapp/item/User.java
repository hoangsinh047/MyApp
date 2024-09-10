package com.example.myapp.item;

public class User {
    public String username;
    public String phone;

    // Constructor mặc định
    public User() {
    }

    // Constructor với tham số
    public User(String username, String phone) {
        this.username = username;
        this.phone = phone;
    }
}
