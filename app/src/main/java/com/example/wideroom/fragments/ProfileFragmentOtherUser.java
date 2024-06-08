package com.example.wideroom.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.wideroom.activities.MainActivity;
import com.example.wideroom.R;
import com.example.wideroom.models.UserModel;
import com.example.wideroom.utils.AndroidUtil;
import com.example.wideroom.utils.FirebaseUtil;

/**
 * This class is used to show the profile of a other user.
 *
 * Copyright © 2024 Alejandro Recarte Rebollo & Inés Rodrigues Trigo. CC BY-NC (Attribution-NonCommercial)
 *
 * @author Alejandro Recarte Rebollo <alejandro.recarte.rebollo@gmail.com>+
 * @author Inés Rodrigues Trigo <itralways@gmail.com>
 *
 * @version 1.0
 * @date 08-06-2024
 */

public class ProfileFragmentOtherUser extends Fragment {

    ImageView profilePic;
    TextView usernameInput;
    TextView bioInput;
    Button deleteFriendButton;
    UserModel otherUserModel;
    ActivityResultLauncher<Intent> imagePickLauncher;
    Uri selectedImageUri;

    /**
     * Empty constructor.
     */
    public ProfileFragmentOtherUser() {
    }

    /**
     * Called on the first time the fragment is created.
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imagePickLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
            if(result.getResultCode() == Activity.RESULT_OK){
                Intent data = result.getData();
                if(data!=null && data.getData()!=null){
                    selectedImageUri = data.getData();
                    AndroidUtil.setProfilePic(getContext(), selectedImageUri, profilePic);
                }
            }
        });
    }

    /**
     * Called on the first time the view is created.
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_other_user, container, false);
        profilePic = view.findViewById(R.id.profile_image_view_other_user);
        usernameInput = view.findViewById(R.id.profile_username_other_user);
        bioInput = view.findViewById(R.id.profile_bio_other_user);
        deleteFriendButton = view.findViewById(R.id.delete_friend_btn);
        Bundle args = getArguments();
        if (args != null){
            otherUserModel= (UserModel) args.getSerializable("otherUserModel");
            setUserData();
        }
        deleteFriendButton.setOnClickListener(v -> {
            FirebaseUtil.removeFriend(otherUserModel.getUserId());
            AndroidUtil.showToast(getContext(), getContext().getResources().getString(R.string.delete_friend));
            Intent intent = new Intent(getContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
        return view;
    }

    /**
     * Sets the user data on the UI.
     */
    void setUserData(){
        FirebaseUtil.getOtherProfilePicStorageRef(otherUserModel.getUserId()).getDownloadUrl()
                        .addOnCompleteListener(task -> {
                            if(task.isSuccessful()){
                                Uri uri = task.getResult();
                                AndroidUtil.setProfilePic(getContext(), uri, profilePic);
                            }
                        });
        usernameInput.setText(otherUserModel.getUsername());
        bioInput.setText(otherUserModel.getBio());
    }
}