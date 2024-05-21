package com.example.wideroom.models;

import com.google.firebase.Timestamp;

public class FriendModel {

    boolean requestAccepted;
    Timestamp timestamp;
    String userId;
    boolean sender;

    public FriendModel(boolean requestAccepted, Timestamp timestamp, String userId, boolean sender) {
        this.requestAccepted = requestAccepted;
        this.timestamp = timestamp;
        this.userId = userId;
        this.sender = sender;
    }

    public boolean isSender() {
        return sender;
    }

    public void setSender(boolean sender) {
        this.sender = sender;
    }

    public boolean isRequestAccepted() {
        return requestAccepted;
    }

    public void setRequestAccepted(boolean requestAccepted) {
        this.requestAccepted = requestAccepted;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
