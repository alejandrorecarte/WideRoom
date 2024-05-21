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

public class ChatFragment extends Fragment {

    RecyclerView recyclerView;
    RecentChatRecyclerAdapter adapter;
    LinearLayout noResults;
    ProgressBar progressBar;
    public ChatFragment() {
    }


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

    void setupRecyclerView(){
        Log.i("FriendRequestInfo","entra en el recycler");
        FirebaseUtil.allOwnFriendsReference()
                .whereEqualTo("requestAccepted",true)
                .get().addOnCompleteListener(task ->{
                    if(task.isSuccessful()) {
                        List<String> friends = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            friends.add(document.getString("userId"));
                        }

                        if(friends.size() != 0) {
                            Query query = FirebaseUtil.allChatroomCollectionReference()
                                    .whereArrayContainsAny("userIds", friends)
                                    .whereArrayContains("userIds", FirebaseUtil.currentUserId())
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
                        }else{
                            setInProgress(1);
                        }
                    }
                });
    }

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

    @Override
    public void onStart() {
        super.onStart();
        if(adapter!=null){
            adapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if(adapter!=null){
            adapter.stopListening();
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        if(adapter!=null){
            adapter.notifyDataSetChanged();
        }
    }
}