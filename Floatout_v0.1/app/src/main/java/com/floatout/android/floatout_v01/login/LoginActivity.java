package com.floatout.android.floatout_v01.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.floatout.android.floatout_v01.MainActivity;
import com.floatout.android.floatout_v01.PasswordResetActivity;
import com.floatout.android.floatout_v01.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private String LOG_TAG = LoginActivity.class.getSimpleName();

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private EditText mEditTextEmailInput, mEditTextPasswordInput;

    private Button singnIngButton;

    private TextView createAccount,passwordReset;

    private String email, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        grabInputData();

        mAuth = FirebaseAuth.getInstance();

        singnIngButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                email = mEditTextEmailInput.getText().toString();
                password = mEditTextPasswordInput.getText().toString();

                Log.v(LOG_TAG, "i'm being clicked bc" );

                email.trim();
                password.trim();
                if(!email.isEmpty() || !password.isEmpty()) {
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {

                                    //Log.d(LOG_TAG, "token= " + task.getResult().getUser().getToken(true));

                                    if (!task.isSuccessful()) {
                                        Log.d(LOG_TAG, "sign in failed" + task.getException().toString());
                                        Toast.makeText(LoginActivity.this, "Check credentials and enter again!", Toast.LENGTH_SHORT)
                                                .show();
                                    }

                                }
                            });
                } else{
                    Toast.makeText(LoginActivity.this, "Credentials are required BC!",Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null){
                    Log.w(LOG_TAG, "user signed in " + user.getUid());
                    Toast.makeText(LoginActivity.this, "Signed in!",Toast.LENGTH_SHORT)
                            .show();

                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                } else {
                    Log.v(LOG_TAG, "user signedout");
                }

            }
        };

        createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, CreateAccountActivity.class);
                startActivity(intent);
            }
        });

        passwordReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent passwordResetIntent = new Intent(LoginActivity.this, PasswordResetActivity.class);
                startActivity(passwordResetIntent);
            }
        });
    }

    @Override
    protected void onResume() { super.onResume();}

    @Override
    public void onPause() {
        super.onPause();
    }

    public void grabInputData() {
        mEditTextEmailInput = (EditText) findViewById(R.id.edit_text_email);
        mEditTextPasswordInput = (EditText) findViewById(R.id.edit_text_password);
        singnIngButton = (Button) findViewById(R.id.signin_with_password);
        createAccount = (TextView) findViewById(R.id.tv_sign_up);
        passwordReset = (TextView) findViewById(R.id.password_reset);
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop(){
        super.onStop();
        if(mAuthListener != null){
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
