package com.example.wideroom.model;

import com.google.firebase.Timestamp;

import java.io.Serializable;

public class UserModel implements Serializable {
    private String phone;
    private String username;
    private Timestamp createdTimestamp;
    private String userId;
    private String oneSignalId;
    private String subscriptionId;
    private String bio;

    public UserModel() {
    }

    public UserModel(String phone, String username, Timestamp createdTimestamp,String userId) {
        this.phone = phone;
        this.username = username;
        this.createdTimestamp = createdTimestamp;
        this.userId = userId;
    }

    public UserModel(String phone, String username, Timestamp createdTimestamp, String userId, String oneSignalId, String subscriptionId, String bio) {
        this.phone = phone;
        this.username = username;
        this.createdTimestamp = createdTimestamp;
        this.userId = userId;
        this.oneSignalId = oneSignalId;
        this.subscriptionId = subscriptionId;
        this.bio = bio;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Timestamp getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(Timestamp createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getOneSignalId() {
        return oneSignalId;
    }

    public void setOneSignalId(String oneSignalId) {
        this.oneSignalId = oneSignalId;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }
}