package com.example.wideroom.models;

import com.google.firebase.Timestamp;
import java.util.List;

/**
 * This class is the model for chatrooms.
 *
 * Copyright © 2024 Alejandro Recarte Rebollo & Inés Rodrigues Trigo. CC BY-NC (Attribution-NonCommercial)
 *
 * @author Alejandro Recarte Rebollo <alejandro.recarte.rebollo@gmail.com>+
 * @author Inés Rodrigues Trigo <itralways@gmail.com>
 *
 * @version 1.0
 * @date 08-06-2024
 */

public class ChatroomModel {
    String chatroomId;
    List<String> userIds;
    Timestamp lastMessageTimestamp;
    String lastMessageSenderId;
    String lastMessage;

    /**
     * Empty constructor.
     */
    public ChatroomModel() {
    }

    /**
     * Parametrized constructor.
     * @param chatroomId
     * @param userIds
     * @param lastMessageTimestamp
     * @param lastMessageSenderId
     */
    public ChatroomModel(String chatroomId, List<String> userIds, Timestamp lastMessageTimestamp, String lastMessageSenderId) {
        this.chatroomId = chatroomId;
        this.userIds = userIds;
        this.lastMessageTimestamp = lastMessageTimestamp;
        this.lastMessageSenderId = lastMessageSenderId;
    }

    /**
     * Returns the chatroom id.
     * @return chatroomId
     */
    public String getChatroomId() {
        return chatroomId;
    }


    /**
     * Sets the user ids of the chatroom.
     * @return userIds
     */
    public List<String> getUserIds() {
        return userIds;
    }

    /**
     * Returns the last message timestamp.
     * @return lastMessageTimestamp
     */
    public Timestamp getLastMessageTimestamp() {
        return lastMessageTimestamp;
    }

    /**
     * Sets the last message timestamp.
     * @param lastMessageTimestamp
     */
    public void setLastMessageTimestamp(Timestamp lastMessageTimestamp) {
        this.lastMessageTimestamp = lastMessageTimestamp;
    }

    /**
     * Returns the last message sender id.
     * @return lastMessageSenderId
     */
    public String getLastMessageSenderId() {
        return lastMessageSenderId;
    }

    /**
     * Sets the last message sender id.
     * @param lastMessageSenderId
     */
    public void setLastMessageSenderId(String lastMessageSenderId) {
        this.lastMessageSenderId = lastMessageSenderId;
    }

    /**
     * Returns the last message.
     * @return lastMessage
     */
    public String getLastMessage() {
        return lastMessage;
    }

    /**
     * Sets the last message.
     * @param lastMessage
     */
    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    /**
     * Sets the chatroom id.
     * @param chatroomId
     */
    public void setChatroomId(String chatroomId) {
        this.chatroomId = chatroomId;
    }

    /**
     * Sets the user ids of the chatroom.
     * @param userIds
     */
    public void setUserIds(List<String> userIds) {
        this.userIds = userIds;
    }
}