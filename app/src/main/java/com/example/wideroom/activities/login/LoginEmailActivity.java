package com.example.wideroom.activities.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.wideroom.R;
import com.example.wideroom.activities.MainActivity;
import com.example.wideroom.utils.AndroidUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginEmailActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    EditText emailInput;
    EditText passwordInput;
    Button loginBtn;
    ProgressBar progressBar;
    Button loginPhoneBtn;
    TextView resetPasswordTextView;
    TextView registerTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_email);

        progressBar = findViewById(R.id.event_progress_bar);

        progressBar.setVisibility(View.GONE);

        loginBtn = findViewById(R.id.login_btn);
        emailInput = findViewById(R.id.login_email);
        passwordInput = findViewById(R.id.login_password);
        registerTextView= findViewById(R.id.register_text);
        resetPasswordTextView= findViewById(R.id.restore_password_text);
        loginPhoneBtn = findViewById(R.id.login_phone_btn);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!emailInput.getText().toString().isEmpty() && !passwordInput.getText().toString().isEmpty()) {
                    signInWithEmail(emailInput.getText().toString(), passwordInput.getText().toString());
                } else {
                    AndroidUtil.showToast(LoginEmailActivity.this, getResources().getString(R.string.fill_all));
                }
            }
        });

        registerTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginEmailActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        resetPasswordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(emailInput.getText().toString().isEmpty()){
                    AndroidUtil.showToast(LoginEmailActivity.this, "Please fill email field");
                    return;
                }
                sendRestorePassword(emailInput.getText().toString());
            }
        });

        loginPhoneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginEmailActivity.this, LoginPhoneNumberActivity.class);
                startActivity(intent);
            }
        });

    }

    private void signInWithEmail(String email, String password) {
        mAuth = FirebaseAuth.getInstance();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // El inicio de sesion fue exitoso
                            FirebaseUser user = mAuth.getCurrentUser();

                            if(user.isEmailVerified()){
                                Intent intent = new Intent(LoginEmailActivity.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
                                startActivity(intent);
                            }else{
                                mAuth.getCurrentUser().sendEmailVerification()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    AndroidUtil.showToast(LoginEmailActivity.this, getResources().getString(R.string.vrf_email_sent));
                                                } else {
                                                    AndroidUtil.showToast(LoginEmailActivity.this, getResources().getString(R.string.error_email_send));
                                                }
                                            }
                                        });
                            }
                        } else {
                            // El inicio de sesion falló
                            AndroidUtil.showToast(LoginEmailActivity.this, getResources().getString(R.string.login_ko));
                        }
                    }
                });
    }

    private void sendRestorePassword(String email){
        mAuth = FirebaseAuth.getInstance();
        mAuth.sendPasswordResetEmail(email)
               .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            AndroidUtil.showToast(LoginEmailActivity.this, getResources().getString(R.string.email_ok));
                        } else {
                            AndroidUtil.showToast(LoginEmailActivity.this, getResources().getString(R.string.email_ko));
                        }
                    }
                });
    }
}