package com.example.wideroom.model;

import com.google.firebase.Timestamp;

public class FriendModel {

    boolean requestAccepted;
    Timestamp timestamp;
    String userId;

    public FriendModel(boolean requestAccepted, Timestamp timestamp, String userId) {
        this.requestAccepted = requestAccepted;
        this.timestamp = timestamp;
        this.userId = userId;
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
