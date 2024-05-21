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

public class FirebaseUtil {

    public static String currentUserId(){
        return FirebaseAuth.getInstance().getUid();
    }

    public static boolean isLoggedIn(){
        if(currentUserId() != null){
            return true;
        }
        return false;
    }

    public static DocumentReference currentUserDetails(){
        return FirebaseFirestore.getInstance().collection("users").document(currentUserId());
    }

    public static CollectionReference allUserCollectionReference(){
        return FirebaseFirestore.getInstance().collection("users");
    }
    public static DocumentReference getChatroomReference(String chatroomId){
        return FirebaseFirestore.getInstance().collection("chatrooms").document(chatroomId);
    }
    public static CollectionReference getChatroomMessageReference(String chatroomId){
        return getChatroomReference(chatroomId).collection("chats");
    }
    public static String getChatroomId(String userId1,String userId2){
        if(userId1.hashCode()<userId2.hashCode()){
            return userId1+"_"+userId2;
        }
        else{
            return userId2+"_"+userId1;
        }
    }
    public static CollectionReference allChatroomCollectionReference(){
        return FirebaseFirestore.getInstance().collection("chatrooms");
    }
    public  static DocumentReference getOtherUserFromChatroom(List<String> userIds){
        if(userIds.get(0).equals(FirebaseUtil.currentUserId())){
            return allUserCollectionReference().document(userIds.get(1));
        }else{
            return allUserCollectionReference().document(userIds.get(0));
        }
    }
    public static String timestampToString(Timestamp timestamp){
        return new SimpleDateFormat("HH:mm").format(timestamp.toDate());
    }

    public static void logout(){
        FirebaseAuth.getInstance().signOut();
    }

    public static StorageReference getCurrentProfilePicStorageRef(){
        return FirebaseStorage.getInstance().getReference().child("profile_pic")
                .child(FirebaseUtil.currentUserId());
    }

    public static StorageReference getOtherProfilePicStorageRef(String otherUserId){
        return FirebaseStorage.getInstance().getReference().child("profile_pic")
                .child(otherUserId);
    }
    public static CollectionReference allEventsCollectionReference(){
        return FirebaseFirestore.getInstance().collection("events");
    }
    public static DocumentReference getEventReference(String eventId){
        return FirebaseFirestore.getInstance().collection("events").document(eventId);
    }

    public static void markAsRead(String chatroomId, UserModel otherUser){
        Query query = FirebaseUtil.getChatroomMessageReference(chatroomId)
                .whereEqualTo("senderId", otherUser.getUserId());
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    // Obtiene la referencia del documento y actualiza el campo isRead a true
                    DocumentReference docRef = document.getReference();
                    docRef.update("read", true);
                }
            } else {
                Log.d("FirebaseUtil Info", "Error al obtener los documentos: ", task.getException());
            }
        });
    }

    public static StorageReference getEventPicIconStorageRef(String eventId){
        return FirebaseStorage.getInstance().getReference().child("event_pic").child("icon")
                .child(eventId);
    }

    public static CollectionReference allEventSubscribersReference(String eventId){
        return getEventReference(eventId).collection("subscribers");
    }

    public static DocumentReference currentUserEventSubscriberReference(String eventId){
        return getEventReference(eventId).collection("subscribers").document(currentUserId());
    }

    public static DocumentReference getEventsSubscriberReference(String eventId, String userId){
        return getEventReference(eventId).collection("subscribers").document(userId);
    }

    public static CollectionReference allOwnFriendsReference(){
        return FirebaseFirestore.getInstance().collection("users").document(currentUserId())
                .collection("friends");
    }

    public static void sendFriendRequest(String userId){
        FriendModel friendModel = new FriendModel(false, Timestamp.now(), currentUserId(), false);
        FirebaseFirestore.getInstance().collection("users").document(userId).collection("friends")
                .document(currentUserId()).set(friendModel);
        friendModel.setSender(true);
        friendModel.setUserId(userId);
        FirebaseFirestore.getInstance().collection("users").document(currentUserId()).collection("friends")
                .document(userId).set(friendModel);
    }

    public static void acceptFriendRequest(String userId){
        FirebaseFirestore.getInstance().collection("users").document(userId).collection("friends")
                .document(currentUserId()).update("requestAccepted", true);
        FirebaseFirestore.getInstance().collection("users").document(currentUserId()).collection("friends")
                .document(userId).update("requestAccepted", true);
    }

    public static void removeFriend(String userId){
        FirebaseFirestore.getInstance().collection("users").document(currentUserId()).collection("friends")
                .document(userId).delete();
        FirebaseFirestore.getInstance().collection("users").document(userId).collection("friends")
                .document(currentUserId()).delete();
    }
}
