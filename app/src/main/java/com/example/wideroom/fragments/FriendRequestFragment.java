package com.example.wideroom.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.wideroom.R;
import com.example.wideroom.adapters.FriendRequestRecyclerAdapter;
import com.example.wideroom.models.EventModel;
import com.example.wideroom.models.UserModel;
import com.example.wideroom.utils.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to show the friend requests of the current user.
 *
 * Copyright © 2024 Alejandro Recarte Rebollo & Inés Rodrigues Trigo. CC BY-NC (Attribution-NonCommercial)
 *
 * @author Alejandro Recarte Rebollo <alejandro.recarte.rebollo@gmail.com>+
 * @author Inés Rodrigues Trigo <itralways@gmail.com>
 *
 * @version 1.0
 * @date 08-06-2024
 */

public class FriendRequestFragment extends Fragment {
    
    RecyclerView recyclerView;
    FriendRequestRecyclerAdapter adapter;
    LinearLayout noResults;
    String eventId;
    EventModel eventModel;

    /**
     * Empty constructor.
     */
    public FriendRequestFragment() {
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
        View view =  inflater.inflate(R.layout.fragment_friend_request, container, false);
        recyclerView = view.findViewById(R.id.recyler_view);
        noResults = view.findViewById(R.id.no_results_found);
        setupRecyclerView();
        return view;
    }

    /**
     * Sets up the recycler view to show the friend requests.
     */
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

                            noResults.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
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