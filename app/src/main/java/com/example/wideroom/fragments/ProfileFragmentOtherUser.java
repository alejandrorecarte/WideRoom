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

public class ProfileFragmentOtherUser extends Fragment {

    ImageView profilePic;
    TextView usernameInput;
    TextView bioInput;
    Button deleteFriendButton;

    UserModel otherUserModel;
    ActivityResultLauncher<Intent> imagePickLauncher;
    Uri selectedImageUri;

    public ProfileFragmentOtherUser() {
    }

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
            getUserData();
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

    void getUserData(){
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