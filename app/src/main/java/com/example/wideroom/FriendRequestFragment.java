package com.example.wideroom;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wideroom.adapter.EventRecyclerAdapter;
import com.example.wideroom.adapter.FriendRequestRecyclerAdapter;
import com.example.wideroom.model.EventModel;
import com.example.wideroom.model.UserModel;
import com.example.wideroom.utils.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class FriendRequestFragment extends Fragment {


    RecyclerView recyclerView;
    FriendRequestRecyclerAdapter adapter;
    String eventId;
    EventModel eventModel;


    public FriendRequestFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_friend_request, container, false);
        recyclerView = view.findViewById(R.id.recyler_view);
        setupRecyclerView();
        return view;
    }

    void setupRecyclerView(){
        Log.i("FriendRequestInfo","entra en el recycler");
        FirebaseUtil.allOwnFriendsReference()
                .whereEqualTo("requestAccepted",false)
                .get().addOnCompleteListener(task ->{
                    if(task.isSuccessful()) {
                        List<String> requesters = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if(!document.getBoolean("sender") == true){
                                requesters.add(document.getString("userId"));
                            }
                        }
                        if(requesters.size()!=0) {
                            Log.i("FriendRequestInfo", "Número de suscriptores encontrados: " + requesters.size()); // Para verificar cuántos suscriptores se han obtenido.
                            Query query = FirebaseUtil.allUserCollectionReference().whereIn("userId", requesters);

                            Log.i("FriendRequestInfo", "Query Firestore creada correctamente: " + query.toString()); // Para verificar la consulta Firestore creada correctamente.
                            FirestoreRecyclerOptions<UserModel> options = new FirestoreRecyclerOptions.Builder<UserModel>()
                                    .setQuery(query, UserModel.class).build();

                            adapter = new FriendRequestRecyclerAdapter(options, getContext());
                            recyclerView.setAdapter(adapter);
                            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                            adapter.startListening();
                        }
                    }
                    Log.e("FriendRequestError","Error al obtener datos de peticiones de amigos" + task.getException());
                });
    }
}