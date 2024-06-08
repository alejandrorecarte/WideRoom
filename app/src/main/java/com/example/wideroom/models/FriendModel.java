package com.example.wideroom.models;

import com.google.firebase.Timestamp;

/**
 * This class is the model for friend requests.
 *
 * Copyright © 2024 Alejandro Recarte Rebollo & Inés Rodrigues Trigo. CC BY-NC (Attribution-NonCommercial)
 *
 * @author Alejandro Recarte Rebollo <alejandro.recarte.rebollo@gmail.com>+
 * @author Inés Rodrigues Trigo <itralways@gmail.com>
 *
 * @version 1.0
 * @date 08-06-2024
 */

public class FriendModel {

    boolean requestAccepted;
    Timestamp timestamp;
    String userId;
    boolean sender;

    /**
     * Empty constructor.
     */
    public FriendModel() {
    }

    /**
     * Parametrized constructor
     * @param requestAccepted
     * @param timestamp
     * @param userId
     * @param sender
     */
    public FriendModel(boolean requestAccepted, Timestamp timestamp, String userId, boolean sender) {
        this.requestAccepted = requestAccepted;
        this.timestamp = timestamp;
        this.userId = userId;
        this.sender = sender;
    }

    /**
     * Sets the sender
     * @param sender
     */
    public void setSender(boolean sender) {
        this.sender = sender;
    }

    /**
     * Returns the friend request timestamp
     * @return timestamp
     */
    public Timestamp getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the friend request timestamp
     * @param timestamp
     */
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Returns the userId
     * @return userId
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the userId
     * @param userId
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Returns if the request is accepted,
     * @return isRequestAccepted
     */
    public boolean isRequestAccepted() {
        return requestAccepted;
    }

    /**
     * Sets if the request is accepted
     * @param requestAccepted
     */
    public void setRequestAccepted(boolean requestAccepted) {
        this.requestAccepted = requestAccepted;
    }

    /**
     * Returns if the request is sent by the current user
     * @return isSender
     */
    public boolean isSender() {
        return sender;
    }
}