package com.example.wideroom.model;

import com.google.firebase.Timestamp;

import java.io.Serializable;

public class EventSubscriptionModel implements Serializable {

    private String userId;
    private Timestamp subTimestamp;
    private boolean isSubscribed;

    public EventSubscriptionModel() {
    }


    public EventSubscriptionModel(String userId, Timestamp subTimestamp) {
        this.userId = userId;
        this.subTimestamp = subTimestamp;
        this.isSubscribed = false;
    }

    public Timestamp getSubTimestamp() {
        return subTimestamp;
    }

    public void setSubTimestamp(Timestamp subTimestamp) {
        this.subTimestamp = subTimestamp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isSubscribed() {
        return isSubscribed;
    }

    public void setSubscribed(boolean subscribed) {
        isSubscribed = subscribed;
    }
}
