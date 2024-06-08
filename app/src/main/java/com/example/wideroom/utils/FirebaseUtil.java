package com.example.wideroom.utils;

import android.util.Log;
import com.example.wideroom.models.FriendModel;
import com.example.wideroom.models.UserModel;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * This class is used to manage the Firebase database.
 *
 * Copyright © 2024 Alejandro Recarte Rebollo & Inés Rodrigues Trigo. CC BY-NC (Attribution-NonCommercial)
 *
 * @author Alejandro Recarte Rebollo <alejandro.recarte.rebollo@gmail.com>+
 * @author Inés Rodrigues Trigo <itralways@gmail.com>
 *
 * @version 1.0
 * @date 08-06-2024
 */

public class FirebaseUtil {

    /**
     * Returns the current user id.
     * @return userId
     */
    public static String currentUserId(){
        return FirebaseAuth.getInstance().getUid();
    }

    /**
     * Returns if the user is logged in or not.
     * @return isLoggedIn
     */
    public static boolean isLoggedIn(){
        if(currentUserId() != null){
            return true;
        }
        return false;
    }

    /**
     * Returns the current user details.
     * @return user
     */
    public static DocumentReference currentUserDetails(){
        return FirebaseFirestore.getInstance().collection("users").document(currentUserId());
    }

    /**
     * Returns all users information.
     * @return usersCollection
     */
    public static CollectionReference allUserCollectionReference(){
        return FirebaseFirestore.getInstance().collection("users");
    }

    /**
     * Returns the chatroom reference for the given chatroom id.
     * @param chatroomId
     * @return chatroomReference
     */
    public static DocumentReference getChatroomReference(String chatroomId){
        return FirebaseFirestore.getInstance().collection("chatrooms").document(chatroomId);
    }

    /**
     * Returns the chatroom message reference for the given chatroom id.
     * @param chatroomId
     * @return chatroomMessageReference
     */
    public static CollectionReference getChatroomMessageReference(String chatroomId){
        return getChatroomReference(chatroomId).collection("chats");
    }

    /**
     * Returns the chatroom id for the users ids.
     * @param userId1
     * @param userId2
     * @return chatroomId
     */
    public static String getChatroomId(String userId1,String userId2){
        if(userId1.hashCode()<userId2.hashCode()){
            return userId1+"_"+userId2;
        }
        else{
            return userId2+"_"+userId1;
        }
    }

    /**
     * Returns all chatrooms information.
     * @return chatroomCollectionReference
     */
    public static CollectionReference allChatroomCollectionReference(){
        return FirebaseFirestore.getInstance().collection("chatrooms");
    }

    /**
     * Returns the other user from the chatroom.
     * @param userIds
     * @return otherUser
     */
    public  static DocumentReference getOtherUserFromChatroom(List<String> userIds){
        if(userIds.get(0).equals(FirebaseUtil.currentUserId())){
            return allUserCollectionReference().document(userIds.get(1));
        }else{
            return allUserCollectionReference().document(userIds.get(0));
        }
    }

    /**
     * Returns a Firebase timestamp to a String.
     * @param timestamp
     * @return timestampString
     */
    public static String timestampToString(Timestamp timestamp){
        return new SimpleDateFormat("HH:mm").format(timestamp.toDate());
    }

    /**
     * Allows the user to logout from the app.
     */
    public static void logout(){
        FirebaseAuth.getInstance().signOut();
    }

    /**
     * Returns the current user's profile pic.
     * @return currentProfilePicStorageRef
     */
    public static StorageReference getCurrentProfilePicStorageRef(){
        return FirebaseStorage.getInstance().getReference().child("profile_pic")
                .child(FirebaseUtil.currentUserId());
    }

    /**
     * Returns the other user's profile pic.
     * @param otherUserId
     * @return otherProfilePicStorageRef
     */
    public static StorageReference getOtherProfilePicStorageRef(String otherUserId){
        return FirebaseStorage.getInstance().getReference().child("profile_pic")
                .child(otherUserId);
    }

    /**
     * Returns all the events information.
     * @return allEventsCollectionReference
     */
    public static CollectionReference allEventsCollectionReference(){
        return FirebaseFirestore.getInstance().collection("events");
    }

    /**
     * Returns an event reference for the given event id.
     * @param eventId
     * @return eventReference
     */
    public static DocumentReference getEventReference(String eventId){
        return FirebaseFirestore.getInstance().collection("events").document(eventId);
    }

    /**
     * Marks as read all the messages of the chatroom for our user.
     * @param chatroomId
     * @param otherUser
     */
    public static void markAsRead(String chatroomId, UserModel otherUser){
        Query query = FirebaseUtil.getChatroomMessageReference(chatroomId)
                .whereEqualTo("senderId", otherUser.getUserId());
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    // gets the document reference and updates the isRead field to true
                    DocumentReference docRef = document.getReference();
                    docRef.update("read", true);
                }
            } else {
                Log.d("FirebaseUtil Info", "Error al obtener los documentos: ", task.getException());
            }
        });
    }

    /**
     * Returns all the event pics storage reference.
     * @param eventId
     * @return eventPicsStorageRef
     */
    public static StorageReference getEventPicIconStorageRef(String eventId){
        return FirebaseStorage.getInstance().getReference().child("event_pic").child("icon")
                .child(eventId);
    }

    /**
     * Returns all the event subscribers for the given event id.
     * @param eventId
     * @return eventSubscribersCollectionReference
     */
    public static CollectionReference allEventSubscribersReference(String eventId){
        return getEventReference(eventId).collection("subscribers");
    }

    /**
     * Returns the event subscriber reference for the given event id and user id.
     * @param eventId
     * @param userId
     * @return eventSubscribersDocumentReference
     */
    public static DocumentReference getEventsSubscriberReference(String eventId, String userId){
        return getEventReference(eventId).collection("subscribers").document(userId);
    }

    /**
     * Returns all the user's friends
     * @return friendCollectionReference
     */

    public static CollectionReference allOwnFriendsReference(){
        return FirebaseFirestore.getInstance().collection("users").document(currentUserId())
                .collection("friends");
    }

    /**
     * Sends a friend request for the given user id.
     * @param userId
     */
    public static void sendFriendRequest(String userId){
        FriendModel friendModel = new FriendModel(false, Timestamp.now(), currentUserId(), false);
        FirebaseFirestore.getInstance().collection("users").document(userId).collection("friends")
                .document(currentUserId()).set(friendModel);
        friendModel.setSender(true);
        friendModel.setUserId(userId);
        FirebaseFirestore.getInstance().collection("users").document(currentUserId()).collection("friends")
                .document(userId).set(friendModel);
    }

    /**
     * Accepts a friend request for the given user id.
     * @param userId
     */
    public static void acceptFriendRequest(String userId){
        FirebaseFirestore.getInstance().collection("users").document(userId).collection("friends")
                .document(currentUserId()).update("requestAccepted", true);
        FirebaseFirestore.getInstance().collection("users").document(currentUserId()).collection("friends")
                .document(userId).update("requestAccepted", true);
    }

    /**
     * Removes the friend document for the given user id.
     * @param userId
     */
    public static void removeFriend(String userId){
        FirebaseFirestore.getInstance().collection("users").document(currentUserId()).collection("friends")
                .document(userId).delete();
        FirebaseFirestore.getInstance().collection("users").document(userId).collection("friends")
                .document(currentUserId()).delete();
    }
}