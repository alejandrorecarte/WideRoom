package com.example.wideroom.adapters;

import android.content.Context;
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
import com.example.wideroom.R;
import com.example.wideroom.models.UserModel;
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

/**
 * This class is used to show every friedn request that the current user has received.
 *
 * Copyright © 2024 Alejandro Recarte Rebollo & Inés Rodrigues Trigo. CC BY-NC (Attribution-NonCommercial)
 *
 * @author Alejandro Recarte Rebollo <alejandro.recarte.rebollo@gmail.com>+
 * @author Inés Rodrigues Trigo <itralways@gmail.com>
 *
 * @version 1.0
 * @date 08-06-2024
 */

public class FriendRequestRecyclerAdapter extends FirestoreRecyclerAdapter<UserModel, FriendRequestRecyclerAdapter.UserModelViewHolder> {

    Context context;
    private static final String ONESIGNAL_APP_ID = "27100f8e-6316-478b-8ba0-a8157f66495b";

    /**
     * Parametrized constructor.
     * @param options
     * @param context
     */
    public FriendRequestRecyclerAdapter(@NonNull FirestoreRecyclerOptions<UserModel> options, Context context) {
        super(options);
        this.context = context;
    }

    /**
    * Called when RecyclerView needs a new RecyclerView.ViewHolder of the given type to represent an item.
     * @param holder
     * @param position
     * @param model
     */
    @Override
    protected void onBindViewHolder(UserModelViewHolder holder, int position, @NonNull UserModel model) {
        holder.usernameText.setText(model.getUsername());
        holder.bioText.setText(model.getBio());
        FirebaseUtil.getOtherProfilePicStorageRef(model.getUserId()).getDownloadUrl()
                .addOnCompleteListener(t -> {
                    if(t.isSuccessful()){
                        Uri uri = t.getResult();
                        AndroidUtil.setProfilePic(context, uri, holder.profilePic);
                    }
                });
        holder.acceptButton.setOnClickListener(v -> {
            FirebaseUtil.acceptFriendRequest(model.getUserId());
            sendNotification(context.getString(R.string.friend_request_accepted), model);
            holder.acceptButton.setText(context.getResources().getString(R.string.accepted));
            holder.acceptButton.setEnabled(false);
            holder.declineButton.setVisibility(View.GONE);
        });
        holder.declineButton.setOnClickListener(v -> {
            FirebaseUtil.removeFriend(model.getUserId());
            holder.declineButton.setText(context.getResources().getString(R.string.declined));
            holder.declineButton.setEnabled(false);
            holder.acceptButton.setVisibility(View.GONE);
        });
    }

    /**
     * Called when RecyclerView needs a new RecyclerView.ViewHolder of the given type to represent an item. 
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return
     */
    @NonNull
    @Override
    public UserModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.friend_request_recycler_row, parent, false);
        return new UserModelViewHolder(view);
    }

    /**
     * 
     * Establishes the relationship between the recycler view java items and layout
     */
    class UserModelViewHolder extends RecyclerView.ViewHolder{
        TextView usernameText;
        TextView bioText;
        ImageView profilePic;
        Button acceptButton;
        Button declineButton;

        /**
         * 
         * @param itemView
         */
        public UserModelViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameText = itemView.findViewById(R.id.user_name_text);
            bioText = itemView.findViewById(R.id.user_bio_text);
            profilePic = itemView.findViewById(R.id.profile_pic_image_view);
            acceptButton = itemView.findViewById(R.id.accept_btn);
            declineButton = itemView.findViewById(R.id.reject_btn);
        }
    }

    /**
     * Sends notification of the accepted friendship request via de OneSginal API
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
                        data.put("activity", "SearchUserActivity");
                        notification.put("data", data);
                        notification.put("target_channel", "push");
                        JSONObject contents = new JSONObject();
                        contents.put("en", currentUserModel.getUsername() + " " +message);
                        notification.put("contents", contents);
                        JSONObject headings = new JSONObject();
                        headings.put("en", "Friend request");
                        notification.put("headings",headings);
                        // creates the http connection
                        URL url = new URL("https://onesignal.com/api/v1/notifications");
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestProperty("accept", "application/json");
                        conn.setRequestProperty("content-type", "application/json");
                        conn.setDoOutput(true);
                        // writes the data in the connection
                        OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
                        writer.write(String.valueOf(notification));
                        writer.flush();
                        writer.close();
                        // verifies the server response
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