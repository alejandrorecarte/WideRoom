package com.example.wideroom.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.wideroom.R;
import com.example.wideroom.fragments.ProfileFragmentOtherUser;
import com.example.wideroom.adapters.ChatRecyclerAdapter;
import com.example.wideroom.models.ChatMessageModel;
import com.example.wideroom.models.ChatroomModel;
import com.example.wideroom.models.UserModel;
import com.example.wideroom.utils.AndroidUtil;
import com.example.wideroom.utils.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

/**
 * This class is used to show the user the chatroom with a friend.
 *
 * Copyright © 2024 Alejandro Recarte Rebollo & Inés Rodrigues Trigo. CC BY-NC (Attribution-NonCommercial)
 *
 * @author Alejandro Recarte Rebollo <alejandro.recarte.rebollo@gmail.com>+
 * @author Inés Rodrigues Trigo <itralways@gmail.com>
 *
 * @version 1.0
 * @date 08-06-2024
 */

public class ChatActivity extends AppCompatActivity {

    UserModel otherUser;
    String chatroomId;
    ChatRecyclerAdapter adapter;
    ChatroomModel chatroomModel;
    EditText messageInput;
    ImageButton sendMessageBtn;
    ImageButton backBtn;
    TextView otherUsername;
    RecyclerView recyclerView;
    ImageView imageView;
    ProfileFragmentOtherUser profileFragment;
    FragmentContainerView profileContainer;
    private static final String ONESIGNAL_APP_ID = "27100f8e-6316-478b-8ba0-a8157f66495b";

    /**
     * Called when the activity is first created.
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        otherUser= AndroidUtil.getUserModelFromIntent(getIntent());
        chatroomId = FirebaseUtil.getChatroomId(FirebaseUtil.currentUserId(), otherUser.getUserId());
        messageInput = findViewById(R.id.chat_message_input);
        sendMessageBtn = findViewById(R.id.message_send_btn);
        backBtn = findViewById(R.id.back_btn);
        otherUsername = findViewById(R.id.other_username);
        recyclerView = findViewById(R.id.chat_recycler_view);
        imageView = findViewById(R.id.profile_pic_image_view);
        profileFragment=new ProfileFragmentOtherUser();
        profileContainer = findViewById(R.id.fragment_container);
        Bundle args = new Bundle();
        args.putSerializable("otherUserModel", otherUser);
        profileFragment.setArguments(args);
        profileContainer.setVisibility(View.GONE);
        // recupera la imagen del otro usuario
        try {
            FirebaseUtil.getOtherProfilePicStorageRef(otherUser.getUserId()).getDownloadUrl()
                    .addOnCompleteListener(t -> {
                        if (t.isSuccessful()) {
                            Uri uri = t.getResult();
                            AndroidUtil.setProfilePic(this, uri, imageView);
                        }
                    });
        }catch(Exception e){}
        // si se hace click sobre la imagen abre profileFragment para ver los detalles del otor usuario
        imageView.setOnClickListener(v -> {
            if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.add(R.id.fragment_container, profileFragment).addToBackStack(null).commit();
                profileContainer.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                messageInput.setVisibility(View.GONE);
                sendMessageBtn.setVisibility(View.GONE);
            }
        });
        // si se hace click sobre el nombre de usuario abre profileFragement para ver los detalles del otro usuario
        otherUsername.setOnClickListener(v -> {
            if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.add(R.id.fragment_container, profileFragment).addToBackStack(null).commit();
                profileContainer.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                messageInput.setVisibility(View.GONE);
                sendMessageBtn.setVisibility(View.GONE);
            }
        });
        // botón de vuelta. Vuelve a la activity anterior
        backBtn.setOnClickListener(v -> {
            if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                Intent intent = new Intent(ChatActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else {
                getSupportFragmentManager().popBackStack();
                profileContainer.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                messageInput.setVisibility(View.VISIBLE);
                sendMessageBtn.setVisibility(View.VISIBLE);
            }
        });
        otherUsername.setText(otherUser.getUsername());
        // envía el mensaje
        sendMessageBtn.setOnClickListener((v -> {
            String message = messageInput.getText().toString().trim();
            if(message.isEmpty())
                return;
            sendMessageToUser(message);
        }));
        getOrCreateChatroomModel();
        setupChatRecyclerView();
    }

    /**
     * Establishes the recycler view of the chat, updates the chat if needed
     */
    void setupChatRecyclerView(){
        FirebaseUtil.markAsRead(chatroomId, otherUser);
        Query query = FirebaseUtil.getChatroomMessageReference(chatroomId)
                .orderBy("timestamp", Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<ChatMessageModel> options = new FirestoreRecyclerOptions.Builder<ChatMessageModel>()
                .setQuery(query, ChatMessageModel.class).build();
        adapter = new ChatRecyclerAdapter(options, ChatActivity.this);
        LinearLayoutManager manager=new LinearLayoutManager(this);
        manager.setReverseLayout(true);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
        adapter.startListening();
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                recyclerView.smoothScrollToPosition(0);
                adapter.notifyDataSetChanged();
                FirebaseUtil.markAsRead(chatroomId, otherUser);
            }
        });
    }

    /**
     * Sends the message and notification to the other user. Updates the DB
     * @param message
     */
    void sendMessageToUser(String message){
        chatroomModel.setLastMessageTimestamp(Timestamp.now());
        chatroomModel.setLastMessageSenderId(FirebaseUtil.currentUserId());
        chatroomModel.setLastMessage(message);
        FirebaseUtil.getChatroomReference(chatroomId).set(chatroomModel);
        ChatMessageModel chatMessageModel = new ChatMessageModel(message, FirebaseUtil.currentUserId(), Timestamp.now(), false);
        FirebaseUtil.getChatroomMessageReference(chatroomId).add(chatMessageModel)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if(task.isSuccessful()){
                            messageInput.setText("");
                            try {
                                sendNotification(message, otherUser);
                                adapter.notifyDataSetChanged();
                            }catch(Exception e){
                                Log.e("OneSignal Response", Log.getStackTraceString(e));
                            }
                        }
                    }
                });
    }

    /**
     * Gets or creates a chatroom where the chats will be saved with the usersIds
     */
    void getOrCreateChatroomModel(){
        FirebaseUtil.getChatroomReference(chatroomId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    chatroomModel = task.getResult().toObject(ChatroomModel.class);
                    if(chatroomModel==null){
                        chatroomModel = new ChatroomModel(
                                chatroomId,
                                Arrays.asList(FirebaseUtil.currentUserId(), otherUser.getUserId()),
                                Timestamp.now(),
                                ""
                        );
                        FirebaseUtil.getChatroomReference(chatroomId).set(chatroomModel);
                    }
                }
            }
        });
    }

    /**
     * Sends notification via OneSignal API
     * @param message
     * @param otherUser
     */
    private static void sendNotification(String message, UserModel otherUser) {
        FirebaseUtil.currentUserDetails().get().addOnCompleteListener(task -> {
            UserModel currentUserModel = task.getResult().toObject(UserModel.class);
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    try {
                        JSONObject notification = new JSONObject();
                        notification.put("app_id", ONESIGNAL_APP_ID);
                        JSONArray subscriptionIds = new JSONArray();
                        Log.i("OneSignal Response", "Sending notification to " + otherUser.getSubscriptionId());
                        subscriptionIds.put(otherUser.getSubscriptionId());
                        notification.put("include_subscription_ids",subscriptionIds);
                        JSONObject data = new JSONObject();
                        data.put("userId", currentUserModel.getUserId());
                        data.put("activity", "ChatActivity");
                        notification.put("data", data);
                        notification.put("target_channel", "push");
                        JSONObject contents = new JSONObject();
                        contents.put("en", message);
                        notification.put("contents", contents);
                        JSONObject headings = new JSONObject();
                        headings.put("en", currentUserModel.getUsername());
                        notification.put("headings",headings);
                        // creates the http connection
                        URL url = new URL("https://onesignal.com/api/v1/notifications");
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestProperty("accept", "application/json");
                        conn.setRequestProperty("content-type", "application/json");
                        conn.setDoOutput(true);
                        // writes the data on the connection
                        OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
                        writer.write(String.valueOf(notification));
                        writer.flush();
                        writer.close();
                        // verifies the response
                        int responseCode = conn.getResponseCode();
                        Log.i("OneSignal Response", String.valueOf(responseCode));
                        if (responseCode != HttpURLConnection.HTTP_OK && responseCode != HttpURLConnection.HTTP_CREATED) {
                            Log.e("OneSignal Response",("Error al enviar la notificación. Detalles:"));
                            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                            String line;
                            while ((line = br.readLine()) != null) {
                                Log.e("OneSignal Response", line);
                            }
                        }
                    } catch (Exception e) {
                        Log.e("OneSignal Response", Log.getStackTraceString(e));
                    }
                    return null;
                }
            }.execute();
        });
    }
}