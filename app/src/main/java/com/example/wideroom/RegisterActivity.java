package com.example.wideroom;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.wideroom.model.UserModel;
import com.example.wideroom.utils.AndroidUtil;
import com.example.wideroom.utils.FirebaseUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

public class RegisterActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    EditText usernameInput;
    EditText emailInput;
    EditText passwordInput;
    EditText passwordConfirmInput;
    Spinner languageSpinner;
    Button nextBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        usernameInput = findViewById(R.id.register_username);
        nextBtn = findViewById(R.id.login_next_btn);
        emailInput = findViewById(R.id.register_email);
        passwordInput = findViewById(R.id.register_password);
        passwordConfirmInput = findViewById(R.id.register_password_confirm);


        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!emailInput.getText().toString().isEmpty() && !passwordInput.getText().toString().isEmpty()
                && passwordConfirmInput.getText().toString().equals(passwordInput.getText().toString())
                && passwordInput.getText().toString().length() >= 6
                && usernameInput.getText().toString().length() >= 3) {
                    registerUser(emailInput.getText().toString(), passwordInput.getText().toString());
                } else if (passwordInput.getText().toString().length() < 6) {
                    AndroidUtil.showToast(RegisterActivity.this, getResources().getString(R.string.pwd_6));
                } else if (usernameInput.getText().toString().length() < 3) {
                    AndroidUtil.showToast(RegisterActivity.this, getResources().getString(R.string.username_error));
                } else {
                    AndroidUtil.showToast(RegisterActivity.this, getResources().getString(R.string.fill_all));
                }
            }
        });
    }
    private void registerUser(String email, String password) {
        mAuth = FirebaseAuth.getInstance();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Registro exitoso, actualiza la interfaz de usuario.
                            FirebaseUser user = mAuth.getCurrentUser();
                            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        AndroidUtil.showToast(RegisterActivity.this, getResources().getString(R.string.vrf_email_sent));
                                        UserModel userModel = new UserModel(emailInput.getText().toString(), usernameInput.getText().toString(), Timestamp.now(),FirebaseUtil.currentUserId());
                                        FirebaseUtil.currentUserDetails().set(userModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    finish();
                                                }
                                            }
                                        });

                                    } else {
                                        AndroidUtil.showToast(RegisterActivity.this, getResources().getString(R.string.error_email_send));
                                    }
                                }
                            });
                        } else {
                            // El registro falló, actualiza la interfaz de usuario con el mensaje de error.
                            AndroidUtil.showToast(RegisterActivity.this, getResources().getString(R.string.error_register));
                        }
                    }
                });
    }

}