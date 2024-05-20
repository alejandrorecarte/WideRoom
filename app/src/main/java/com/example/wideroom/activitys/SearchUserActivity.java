package com.example.wideroom.activitys;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wideroom.R;
import com.example.wideroom.adapter.FriendRequestRecyclerAdapter;
import com.example.wideroom.adapter.SearchUserRecyclerAdapter;
import com.example.wideroom.model.UserModel;
import com.example.wideroom.utils.AndroidUtil;
import com.example.wideroom.utils.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SearchUserActivity extends AppCompatActivity {
    EditText searchInput;
    ImageButton searchButton;
    ImageButton backButton;
    RecyclerView recyclerView;
    LinearLayout noResults;
    SearchUserRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_user);

        searchInput=findViewById(R.id.search_username_input);
        searchButton=findViewById(R.id.search_user_btn);
        backButton=findViewById(R.id.back_btn);
        recyclerView=findViewById(R.id.search_user_recycler_view);
        noResults=findViewById(R.id.no_results_found);
        searchInput.requestFocus();

        backButton.setOnClickListener(v -> {
            finish();
        });

        searchButton.setOnClickListener(v -> {
            String searchTerm = searchInput.getText().toString();
            if(searchTerm.isEmpty() || searchTerm.length() < 3){
                searchInput.setError(getResources().getString(R.string.invalid_username));
                return;
            }
            setupSearchRecyclerView(searchTerm);
        });

        setupSearchRecyclerView("");
    }



    void setupSearchRecyclerView(String searchTerm){
        Log.i("FriendRequestInfo","entra en el recycler");
        FirebaseUtil.allOwnFriendsReference()
                .whereEqualTo("requestAccepted",true)
                .get().addOnCompleteListener(task ->{
                    if(task.isSuccessful()) {
                        List<String> friends = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            friends.add(document.getString("userId"));
                        }
                        if(friends.size()!=0) {
                            Log.i("FriendRequestInfo", "Número de suscriptores encontrados: " + friends.size()); // Para verificar cuántos suscriptores se han obtenido.
                            Query query = null;

                            if(searchTerm.equals("")){
                                query = FirebaseUtil.allUserCollectionReference().whereIn("userId", friends);
                            }else {
                                query = FirebaseUtil.allUserCollectionReference().whereIn("userId", friends)
                                        .whereGreaterThanOrEqualTo("username",searchTerm)
                                        .whereLessThanOrEqualTo("username",searchTerm + "\uf8ff");
                            }

                            Log.i("FriendRequestInfo", "Query Firestore creada correctamente: " + query.toString()); // Para verificar la consulta Firestore creada correctamente.
                            FirestoreRecyclerOptions<UserModel> options = new FirestoreRecyclerOptions.Builder<UserModel>()
                                    .setQuery(query, UserModel.class).build();

                            adapter = new SearchUserRecyclerAdapter(options, this);
                            recyclerView.setAdapter(adapter);
                            recyclerView.setLayoutManager(new LinearLayoutManager(this));
                            adapter.startListening();
                        }else{
                            noResults.setVisibility(View.VISIBLE);
                        }
                    }
                    Log.e("FriendRequestError","Error al obtener datos de peticiones de amigos" + task.getException());
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(adapter!=null){
            adapter.startListening();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(adapter!=null){
            adapter.stopListening();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(adapter!=null){
            adapter.startListening();
        }
    }
}