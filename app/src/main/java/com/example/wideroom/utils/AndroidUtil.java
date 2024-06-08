package com.example.wideroom.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.ImageView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.wideroom.models.EventModel;
import com.example.wideroom.models.UserModel;

/**
 * This class is used to manage all android related utilities.
 *
 * Copyright © 2024 Alejandro Recarte Rebollo & Inés Rodrigues Trigo. CC BY-NC (Attribution-NonCommercial)
 *
 * @author Alejandro Recarte Rebollo <alejandro.recarte.rebollo@gmail.com>+
 * @author Inés Rodrigues Trigo <itralways@gmail.com>
 *
 * @version 1.0
 * @date 08-06-2024
 */

public class AndroidUtil {

    /**
     * Shows a toast message on the given context.
     * @param context
     * @param message
     */
    public static void showToast(Context context, String message){
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    /**
     * Passes the user model as an intent extra information.
     * @param intent
     * @param model
     */
    public static void passUserModelAsIntent(Intent intent, UserModel model){
        intent.putExtra("username",model.getUsername());
        intent.putExtra("accessId",model.getAccessId());
        intent.putExtra("userId",model.getUserId());
        intent.putExtra("oneSignalId",model.getOneSignalId());
        intent.putExtra("subscriptionId",model.getSubscriptionId());
        intent.putExtra("bio",model.getBio());
    }

    /**
     * Returns the user model from the intent extra information.
     * @param intent
     * @return userModel
     */
    public static UserModel getUserModelFromIntent(Intent intent){
        UserModel userModel =new UserModel();
        userModel.setUsername(intent.getStringExtra("username"));
        userModel.setAccessId(intent.getStringExtra("accessId"));
        userModel.setUserId(intent.getStringExtra("userId"));
        userModel.setOneSignalId(intent.getStringExtra("oneSignalId"));
        userModel.setSubscriptionId(intent.getStringExtra("subscriptionId"));
        userModel.setBio(intent.getStringExtra("bio"));
        return userModel;
    }

    /**
     * Sets the profile picture on the given image view.
     * @param context
     * @param imageUri
     * @param imageView
     */
    public static void setProfilePic(Context context, Uri imageUri, ImageView imageView){
        Glide.with(context).load(imageUri).apply(RequestOptions.circleCropTransform()).into(imageView);
    }

    /**
     * Passes the event model as an intent extra information.
     * @param intent
     * @param model
     */
    public static void passEventModelAsIntent(Intent intent, EventModel model){
        intent.putExtra("eventName",model.getEventName());
        intent.putExtra("date",model.getDate());
        intent.putExtra("eventId",model.getEventId());
        intent.putExtra("city",model.getCity());
        intent.putExtra("address",model.getAddress());
        intent.putExtra("distanceInM",model.getDistanceInM());
        intent.putExtra("lng", model.getLng());
        intent.putExtra("lat", model.getLat());
        intent.putExtra("category",model.getCategory());
    }

    /**
     * Returns the event model from the intent extra information.
     * @param intent
     * @return eventModel
     */
    public static EventModel getEventModelFromIntent(Intent intent){
        EventModel eventModel =new EventModel();
        eventModel.setEventName(intent.getStringExtra("eventName"));
        eventModel.setDate(intent.getStringExtra("date"));
        eventModel.setEventId(intent.getStringExtra("eventId"));
        eventModel.setCity(intent.getStringExtra("city"));
        eventModel.setAddress(intent.getStringExtra("address"));
        eventModel.setDistanceInM(intent.getDoubleExtra("distanceInM",0));
        eventModel.setLng(intent.getDoubleExtra("lng",0));
        eventModel.setLat(intent.getDoubleExtra("lat",0));
        eventModel.setCategory(intent.getStringExtra("category"));
        return eventModel;
    }

    /**
     * Sets the event picture on the given image view.
     * @param context
     * @param imageUri
     * @param imageView
     */
    public static void setEventPic(Context context, Uri imageUri, ImageView imageView){
        Glide.with(context).load(imageUri).into(imageView);
    }
}