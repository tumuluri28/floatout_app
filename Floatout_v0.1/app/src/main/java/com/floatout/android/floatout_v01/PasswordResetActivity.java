package com.floatout.android.floatout_v01;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.floatout.android.floatout_v01.login.LoginActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class PasswordResetActivity extends AppCompatActivity {

    private Button resetPassword;
    private ImageButton backToLogin;
    private EditText emailId;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_reset);

        resetPassword = (Button) findViewById(R.id.btn_reset_password);
        backToLogin = (ImageButton) findViewById(R.id.backto_login_activity);
        emailId = (EditText) findViewById(R.id.edit_text_email);

        mAuth = FirebaseAuth.getInstance();

        backToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent backtoLoginActivityIntent = new Intent(PasswordResetActivity.this, LoginActivity.class);
                startActivity(backtoLoginActivityIntent);
            }
        });

        resetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailId.getText().toString();
                    mAuth.sendPasswordResetEmail(email).addOnCompleteListener(PasswordResetActivity.this, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(!task.isSuccessful()){
                                Toast.makeText(PasswordResetActivity.this, "Email doesn't exisit in our records. RENTER!", Toast.LENGTH_SHORT)
                                        .show();
                            }else{
                                Toast.makeText(PasswordResetActivity.this, "Success. Check your email!", Toast.LENGTH_SHORT)
                                        .show();
                                Intent backtoLoginActivityIntent = new Intent(PasswordResetActivity.this, LoginActivity.class);
                                startActivity(backtoLoginActivityIntent);
                            }
                        }
                    });
            }
        });
    }
}
