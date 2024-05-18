package com.example.wideroom.model;

import com.google.firebase.Timestamp;

import java.io.Serializable;

public class UserModel implements Serializable {
    private String accessId;
    private String username;
    private Timestamp createdTimestamp;
    private String userId;
    private String oneSignalId;
    private String subscriptionId;
    private String bio;
    private String language;

    public UserModel() {
    }

    public UserModel(String accessId, String username, Timestamp createdTimestamp,String userId, String language) {
        this.accessId = accessId;
        this.username = username;
        this.createdTimestamp = createdTimestamp;
        this.userId = userId;
        this.language = language;
    }

    public UserModel(String accessId, String username, Timestamp createdTimestamp, String userId, String oneSignalId, String subscriptionId, String bio, String language) {
        this.accessId = accessId;
        this.username = username;
        this.createdTimestamp = createdTimestamp;
        this.userId = userId;
        this.oneSignalId = oneSignalId;
        this.subscriptionId = subscriptionId;
        this.bio = bio;
        this.language = language;
    }

    public String getAccessId() {
        return accessId;
    }


    public void setAccessId(String accessId) {
        this.accessId = accessId;
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

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}