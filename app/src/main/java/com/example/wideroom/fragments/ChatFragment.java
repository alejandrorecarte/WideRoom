package com.example.wideroom.fragments;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import com.example.wideroom.R;
import com.example.wideroom.adapters.RecentChatRecyclerAdapter;
import com.example.wideroom.models.ChatroomModel;
import com.example.wideroom.utils.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to show the chats of the current user.
 *
 * Copyright © 2024 Alejandro Recarte Rebollo & Inés Rodrigues Trigo. CC BY-NC (Attribution-NonCommercial)
 *
 * @author Alejandro Recarte Rebollo <alejandro.recarte.rebollo@gmail.com>+
 * @author Inés Rodrigues Trigo <itralways@gmail.com>
 *
 * @version 1.0
 * @date 08-06-2024
 */

public class ChatFragment extends Fragment {

    RecyclerView recyclerView;
    RecentChatRecyclerAdapter adapter;
    LinearLayout noResults;
    ProgressBar progressBar;

    /**
     * Empty constructor.
     */
    public ChatFragment() {
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
        View view =  inflater.inflate(R.layout.fragment_chat, container, false);
        recyclerView = view.findViewById(R.id.recyler_view);
        noResults = view.findViewById(R.id.no_results_found);
        progressBar = view.findViewById(R.id.progress_bar);
        setupRecyclerView();
        return view;
    }

    /**
     * Sets up the recycler view to show the chatrooms
     */
    void setupRecyclerView(){
        Log.i("FriendRequestInfo","entra en el recycler");
        FirebaseUtil.allOwnFriendsReference()
                .whereEqualTo("requestAccepted", true)
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> friends = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            friends.add(document.getString("userId"));
                        }
                        if (friends.size() != 0) {
                            FirebaseUtil.allChatroomCollectionReference()
                                    .whereArrayContains("userIds", FirebaseUtil.currentUserId())
                                    .get().addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            List<ChatroomModel> chatrooms = new ArrayList<>();
                                            for (QueryDocumentSnapshot document : task1.getResult()) {
                                                ChatroomModel chatroom = document.toObject(ChatroomModel.class);
                                                for (String friendId : friends) {
                                                    if (chatroom.getUserIds().contains(friendId)) {
                                                        chatrooms.add(chatroom);
                                                        break;
                                                    }
                                                }
                                            }
                                            if (!chatrooms.isEmpty()) {
                                                List<String> chatroomIds = new ArrayList<>();
                                                for (ChatroomModel chatroom : chatrooms) {
                                                    chatroomIds.add(chatroom.getChatroomId());
                                                }
                                                Query query = FirebaseUtil.allChatroomCollectionReference()
                                                        .whereIn("chatroomId", chatroomIds)
                                                        .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING);
                                                FirestoreRecyclerOptions<ChatroomModel> options = new FirestoreRecyclerOptions.Builder<ChatroomModel>()
                                                        .setQuery(query, ChatroomModel.class).build();
                                                setInProgress(2);
                                                adapter = new RecentChatRecyclerAdapter(options, getContext());
                                                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                                                recyclerView.setAdapter(adapter);
                                                adapter.startListening();
                                                adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                                                    @Override
                                                    public void onItemRangeChanged(int positionStart, int itemCount) {
                                                        super.onItemRangeChanged(positionStart, itemCount);
                                                        adapter.notifyDataSetChanged();
                                                    }
                                                });
                                            } else {
                                                setInProgress(1);
                                            }
                                        }
                                    });
                        } else {
                            setInProgress(1);
                        }
                    }
                });
    }

    /**
     * Sets the progress bar and recycler view visibility.
     * inProgress = 0 => progress bar visible, no results found, recycler view invisible
     * inProgress = 1 => progress bar invisible, no results found, recycler view visible
     * inProgress = 2 => progress bar, no results found invisible, recycler view visible
     * @param inProgress
     */
    void setInProgress(int inProgress){
        if(inProgress == 0){
            progressBar.setVisibility(View.VISIBLE);
            noResults.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
        }else if (inProgress == 1){
            progressBar.setVisibility(View.GONE);
            noResults.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }else{
            progressBar.setVisibility(View.GONE);
            noResults.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * On start we need to notify the adapter that the data has changed.
     */
    @Override
    public void onStart() {
        super.onStart();
        if(adapter!=null){
            adapter.startListening();
        }
    }

    /**
     * On stop we need to stop the adapter from listening to data changes.
     */
    @Override
    public void onStop() {
        super.onStop();
        if(adapter!=null){
            adapter.stopListening();
        }
    }

    /**
     * On resume we need to notify the adapter that the data has changed
     */
    @Override
    public void onResume() {
        super.onResume();
        if(adapter!=null){
            adapter.notifyDataSetChanged();
        }
    }
}