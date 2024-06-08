package com.example.wideroom.models;

import com.google.firebase.Timestamp;
import java.io.Serializable;

/**
 * This class is the model for users.
 *
 * Copyright © 2024 Alejandro Recarte Rebollo & Inés Rodrigues Trigo. CC BY-NC (Attribution-NonCommercial)
 *
 * @author Alejandro Recarte Rebollo <alejandro.recarte.rebollo@gmail.com>+
 * @author Inés Rodrigues Trigo <itralways@gmail.com>
 *
 * @version 1.0
 * @date 08-06-2024
 */


public class UserModel implements Serializable {
    private String accessId;
    private String username;
    private Timestamp createdTimestamp;
    private String userId;
    private String oneSignalId;
    private String subscriptionId;
    private String bio;

    /**
     * Empty constructor.
     */
    public UserModel() {
    }

    /**
     * Parametrized constructor.
     * @param accessId
     * @param username
     * @param createdTimestamp
     * @param userId
     */
    public UserModel(String accessId, String username, Timestamp createdTimestamp,String userId) {
        this.accessId = accessId;
        this.username = username;
        this.createdTimestamp = createdTimestamp;
        this.userId = userId;
    }

    /**
     * Parameterized constructor.
     * @param accessId
     * @param username
     * @param createdTimestamp
     * @param userId
     * @param oneSignalId
     * @param subscriptionId
     * @param bio
     */
    public UserModel(String accessId, String username, Timestamp createdTimestamp, String userId, String oneSignalId, String subscriptionId, String bio) {
        this.accessId = accessId;
        this.username = username;
        this.createdTimestamp = createdTimestamp;
        this.userId = userId;
        this.oneSignalId = oneSignalId;
        this.subscriptionId = subscriptionId;
        this.bio = bio;
    }

    /**
     * Returns the accessId.
     * @return accessId
     */
    public String getAccessId() {
        return accessId;
    }

    /**
     * Sets the accessId.
     * @param accessId
     */
    public void setAccessId(String accessId) {
        this.accessId = accessId;
    }

    /**
     * Returns the username.
     * @return username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username.
     * @param username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Returns the userId.
     * @return userId
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the userId.
     * @param userId
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Returns the oneSignalId.
     * @return oneSignalId
     */
    public String getOneSignalId() {
        return oneSignalId;
    }

    /**
     * Sets the oneSignalId.
     * @param oneSignalId
     */
    public void setOneSignalId(String oneSignalId) {
        this.oneSignalId = oneSignalId;
    }

    /**
     * Returns the subscriptionId for OneSignal.
     * @return subscriptionId
     */
    public String getSubscriptionId() {
        return subscriptionId;
    }

    /**
     *  Sets the subscriptionId for OneSignal.
     * @param subscriptionId
     */
    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    /**
     * Returns the user's bio
     * @return bio
     */
    public String getBio() {
        return bio;
    }

    /**
     * Sets the user's bio
     * @param bio
     */
    public void setBio(String bio) {
        this.bio = bio;
    }
}