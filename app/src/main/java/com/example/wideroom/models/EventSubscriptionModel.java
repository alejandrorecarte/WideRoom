package com.example.wideroom.models;

import com.google.firebase.Timestamp;
import java.io.Serializable;

/**
 * This class is the model for events subscriptions.
 *
 * Copyright © 2024 Alejandro Recarte Rebollo & Inés Rodrigues Trigo. CC BY-NC (Attribution-NonCommercial)
 *
 * @author Alejandro Recarte Rebollo <alejandro.recarte.rebollo@gmail.com>+
 * @author Inés Rodrigues Trigo <itralways@gmail.com>
 *
 * @version 1.0
 * @date 08-06-2024
 */

public class EventSubscriptionModel implements Serializable {

    private String userId;
    private Timestamp subTimestamp;
    private boolean isSubscribed;

    /**
     * Empty constructor.
     */
    public EventSubscriptionModel() {
    }

    /**
     * Parametrized constructor
     * @param userId
     * @param subTimestamp
     */
    public EventSubscriptionModel(String userId, Timestamp subTimestamp) {
        this.userId = userId;
        this.subTimestamp = subTimestamp;
        this.isSubscribed = false;
    }

    /**
     * Returns the user id
     * @return userId
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the user id
     * @param userId
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Sets if the user is subscribed to the event
     * @return isSubscribed
     */
    public boolean isSubscribed() {
        return isSubscribed;
    }

    /**
     * Returns if the user is subscribed to the event
     * @param subscribed
     */
    public void setSubscribed(boolean subscribed) {
        isSubscribed = subscribed;
    }
}