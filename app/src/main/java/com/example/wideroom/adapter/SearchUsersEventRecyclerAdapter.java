package com.example.wideroom.adapter;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wideroom.activitys.EventActivity;
import com.example.wideroom.R;
import com.example.wideroom.model.UserModel;
import com.example.wideroom.utils.AndroidUtil;
import com.example.wideroom.utils.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class SearchUsersEventRecyclerAdapter extends FirestoreRecyclerAdapter<UserModel, SearchUsersEventRecyclerAdapter.UserModelViewHolder> {

    Context context;
    private static final String ONESIGNAL_APP_ID = "e16a55f3-93a5-44fa-92fa-cd5d29413fd1";


    public SearchUsersEventRecyclerAdapter(@NonNull FirestoreRecyclerOptions<UserModel> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(UserModelViewHolder holder, int position, @NonNull UserModel model) {
        Log.i("Información","entra en onBindViewHolder con user: " + model.getUsername());
        holder.usernameText.setText(model.getUsername());
        holder.bioText.setText(model.getBio());

        FirebaseUtil.getOtherProfilePicStorageRef(model.getUserId()).getDownloadUrl()
                .addOnCompleteListener(t -> {
                    if(t.isSuccessful()){
                        Uri uri = t.getResult();
                        AndroidUtil.setProfilePic(context, uri, holder.profilePic);
                    }
                });

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, EventActivity.class);
            AndroidUtil.passUserModelAsIntent(intent, model);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        });
        holder.sendRequestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.sendRequestBtn.setText(context.getResources().getString(R.string.requested));
                holder.sendRequestBtn.setEnabled(false);
                sendNotification(context.getResources().getString(R.string.friend_request_notification), model);
                FirebaseUtil.sendFriendRequest(model.getUserId());
            }
        });
    }

    @NonNull
    @Override
    public UserModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.search_users_event_recycler_row, parent, false);
        return new UserModelViewHolder(view);
    }

    class UserModelViewHolder extends RecyclerView.ViewHolder{
        TextView usernameText;
        TextView bioText;
        ImageView profilePic;
        Button sendRequestBtn;

        public UserModelViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameText = itemView.findViewById(R.id.user_name_text);
            bioText = itemView.findViewById(R.id.user_bio_text);
            profilePic = itemView.findViewById(R.id.profile_pic_image_view);
            sendRequestBtn = itemView.findViewById(R.id.send_request_btn);
        }
    }
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
                            subscriptionIds.put(otherUser.getSubscriptionId()); // Asumiendo que getOneSignalId() devuelve el ID de suscripción
                            //onesignalIds.put("onesignal_id", subscriptionIds);
                            notification.put("include_subscription_ids",subscriptionIds);

                            JSONObject data = new JSONObject();
                            data.put("userId", currentUserModel.getUserId());
                            data.put("activity", "FriendRequestFragment");
                            notification.put("data", data);

                            notification.put("target_channel", "push");

                            JSONObject contents = new JSONObject();
                            contents.put("en", currentUserModel.getUsername() + " " +message);

                            notification.put("contents", contents);

                            JSONObject headings = new JSONObject();
                            headings.put("en", "Friend request");
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
