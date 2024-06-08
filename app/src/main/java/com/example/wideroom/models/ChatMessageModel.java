package com.example.wideroom.models;

import com.google.firebase.Timestamp;

/**
 * This class is the model for chat messages.
 *
 * Copyright © 2024 Alejandro Recarte Rebollo & Inés Rodrigues Trigo. CC BY-NC (Attribution-NonCommercial)
 *
 * @author Alejandro Recarte Rebollo <alejandro.recarte.rebollo@gmail.com>+
 * @author Inés Rodrigues Trigo <itralways@gmail.com>
 *
 * @version 1.0
 * @date 08-06-2024
 */

public class ChatMessageModel {
    private String message;
    private String senderId;
    private Timestamp timestamp;
    private boolean isRead;

    /**
     * Empty constructor.
     */
    public ChatMessageModel() {
    }

    /**
     * Parametrized constructor.
     * @param message
     * @param senderId
     * @param timestamp
     * @param isRead
     */
    public ChatMessageModel(String message, String senderId, Timestamp timestamp, boolean isRead) {
        this.message = message;
        this.senderId = senderId;
        this.timestamp = timestamp;
        this.isRead = isRead;
    }

    /**
     * Returns the message
     * @return message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns the sender id ot the message
     * @return senderId
     */
    public String getSenderId() {
        return senderId;
    }


    /**
     * Returns the timestamp of the message.
     * @return timestamp
     */
    public Timestamp getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the timestamp of the message.
     * @param timestamp
     */
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}