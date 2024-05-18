package com.example.wideroom;

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

import com.example.wideroom.adapter.ChatRecyclerAdapter;
import com.example.wideroom.model.ChatMessageModel;
import com.example.wideroom.model.ChatroomModel;
import com.example.wideroom.model.UserModel;
import com.example.wideroom.utils.AndroidUtil;
import com.example.wideroom.utils.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

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

        try {
            FirebaseUtil.getOtherProfilePicStorageRef(otherUser.getUserId()).getDownloadUrl()
                    .addOnCompleteListener(t -> {
                        if (t.isSuccessful()) {
                            Uri uri = t.getResult();
                            AndroidUtil.setProfilePic(this, uri, imageView);
                        }
                    });
        }catch(Exception e){}

        final FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        otherUsername.setOnClickListener(v -> {
            if(getSupportFragmentManager().getBackStackEntryCount() == 0){
                fragmentTransaction.add(R.id.fragment_container, profileFragment).addToBackStack(null).commit();
                profileContainer.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                messageInput.setVisibility(View.GONE);
                sendMessageBtn.setVisibility(View.GONE);
            }
        });

        imageView.setOnClickListener(v -> {
            if(getSupportFragmentManager().getBackStackEntryCount() == 0){
                fragmentTransaction.add(R.id.fragment_container, profileFragment).addToBackStack(null).commit();
                profileContainer.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                messageInput.setVisibility(View.GONE);
                sendMessageBtn.setVisibility(View.GONE);
            }
        });

        backBtn.setOnClickListener(v -> {
            if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                onBackPressed();
            } else {
                getSupportFragmentManager().popBackStack();
                profileContainer.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                messageInput.setVisibility(View.VISIBLE);
                sendMessageBtn.setVisibility(View.VISIBLE);
            }
        });

        otherUsername.setText(otherUser.getUsername());

        sendMessageBtn.setOnClickListener((v -> {
            String message = messageInput.getText().toString().trim();
            if(message.isEmpty())
                return;
            sendMessageToUser(message);
        }));

        getOrCreateChatroomModel();
        setupChatRecyclerView();
    }

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
                FirebaseUtil.markAsRead(chatroomId, otherUser);
            }
        });
    }

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
                            }catch(Exception e){
                                Log.e("OneSignal Response", Log.getStackTraceString(e));
                            }
                        }
                    }
                });

    }

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

    private static void sendNotification(String message, UserModel otherUser) {
        FirebaseUtil.currentUserDetails().get().addOnCompleteListener(task -> {
            UserModel currentUserModel = task.getResult().toObject(UserModel.class);
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    try {
                        JSONObject notification = new JSONObject();
                        notification.put("app_id", "8fb48336-9fc4-45ae-b884-ccda62fd2c3a");

                        JSONArray subscriptionIds = new JSONArray();
                        Log.i("OneSignal Response", "Sending notification to " + otherUser.getSubscriptionId());
                        subscriptionIds.put(otherUser.getSubscriptionId()); // Asumiendo que getOneSignalId() devuelve el ID de suscripción
                        //onesignalIds.put("onesignal_id", subscriptionIds);
                        notification.put("include_subscription_ids",subscriptionIds);

                        JSONObject data = new JSONObject();
                        data.put("userId", currentUserModel.getUserId());
                        notification.put("data", data);

                        notification.put("target_channel", "push");

                        JSONObject contents = new JSONObject();
                        contents.put("en", message);

                        notification.put("contents", contents);

                        JSONObject headings = new JSONObject();
                        headings.put("en", currentUserModel.getUsername());
                        notification.put("headings",headings);


                        // Crear la conexión HTTP
                        URL url = new URL("https://onesignal.com/api/v1/notifications");
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestProperty("accept", "application/json");
                        conn.setRequestProperty("content-type", "application/json");
                        conn.setDoOutput(true);

                        // Escribir los datos en la conexión
                        OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
                        writer.write(String.valueOf(notification));
                        writer.flush();
                        writer.close();

                        // Verificar la respuesta del servidor
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